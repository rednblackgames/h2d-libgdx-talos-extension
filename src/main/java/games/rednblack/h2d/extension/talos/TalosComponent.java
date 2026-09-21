package games.rednblack.h2d.extension.talos;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntSet;
import games.rednblack.talos.runtime.IEmitter;
import games.rednblack.talos.runtime.modules.AbstractModule;
import games.rednblack.talos.runtime.modules.GlobalScopeModule;
import games.rednblack.talos.runtime.Slot;
import games.rednblack.talos.runtime.modules.ColorModule;
import games.rednblack.talos.runtime.modules.EmitterModule;
import games.rednblack.talos.runtime.modules.GradientColorModule;
import games.rednblack.talos.runtime.modules.OffsetModule;
import games.rednblack.talos.runtime.modules.ParticleModule;
import com.badlogic.gdx.utils.IntMap;
import games.rednblack.talos.runtime.values.NumericalValue;

import games.rednblack.editor.renderer.ecs.PooledComponent;
import games.rednblack.talos.runtime.ParticleEffectInstance;
import games.rednblack.talos.runtime.ParticleEffectInstancePool;

public class TalosComponent extends PooledComponent {
    public transient ParticleEffectInstance effect = null;

    /**
     * Values pushed into the global scope slots of the effect, by slot number. A slot is whatever
     * the artist wired a global scope module to inside the effect, so this is how a scene, and a
     * widget state, reach into an effect and change what it does.
     */
    public Array<ScopeValue> scopeValues = new Array<>(0);

    /** False while the emitters are letting their last particles die out instead of making new ones. */
    public transient boolean emitting = true;

    /** Slots the effect actually reads, worked out once from its graph. */
    private transient IntSet usedScopeKeys = null;
    /** What each of those slots is used as, worked out in the same pass. */
    private transient IntMap<ScopeKind> scopeKinds = null;

    /**
     * What a slot holds, as far as the modules reading it tell: a slot declares nothing itself, so
     * its shape comes from where its value goes. Anything read by a module that accepts any shape,
     * or read in two incompatible ways, stays unknown and shows all four numbers.
     */
    public enum ScopeKind {
        NUMBER(1), VECTOR(2), COLOR(4), UNKNOWN(4);

        public final int channels;

        ScopeKind(int channels) {
            this.channels = channels;
        }
    }

    /** Upper bound of the input slot numbers a module declares, all of which are small. */
    private static final int MAX_INPUT_SLOTS = 64;

    /**
     * A slot holds a numerical value, which is up to four numbers: one on its own, two for a
     * position, four for a colour. What a module makes of them is its business, so all four travel.
     */
    public static class ScopeValue {
        public static final int CHANNELS = 4;

        public int key;
        public float[] value = new float[CHANNELS];

        public ScopeValue() {
        }

        public ScopeValue(int key, float[] value) {
            this.key = key;
            set(value);
        }

        public void set(float[] from) {
            for (int i = 0; i < CHANNELS; i++) value[i] = from != null && i < from.length ? from[i] : 0;
        }
    }

    private transient final NumericalValue tmpValue = new NumericalValue();

    /** @param out filled with the four numbers of the slot, all zero if it holds nothing yet */
    public void getScopeValue(int key, float[] out) {
        for (int i = 0; i < scopeValues.size; i++) {
            if (scopeValues.get(i).key == key) {
                System.arraycopy(scopeValues.get(i).value, 0, out, 0, ScopeValue.CHANNELS);
                return;
            }
        }
        for (int i = 0; i < ScopeValue.CHANNELS; i++) out[i] = 0;
    }

    public void setScopeValue(int key, float[] value) {
        for (int i = 0; i < scopeValues.size; i++) {
            if (scopeValues.get(i).key == key) {
                scopeValues.get(i).set(value);
                return;
            }
        }
        scopeValues.add(new ScopeValue(key, value));
    }

    /** Pushes one slot into the effect, which is what makes the modules reading it see the change. */
    public void pushScopeValue(int key, float[] value) {
        if (effect == null) return;
        tmpValue.set(value[0], value[1], value[2], value[3]);
        effect.getScope().setDynamicValue(key, tmpValue);
    }

    /** Pushes every stored value into the effect, for an instance that has just been obtained. */
    public void applyScopeValues() {
        if (effect == null) return;
        for (int i = 0; i < scopeValues.size; i++) {
            pushScopeValue(scopeValues.get(i).key, scopeValues.get(i).value);
        }
    }

    /** @return true if a global scope module of the effect reads this slot */
    public boolean usesScopeKey(int key) {
        if (effect == null) return false;
        inspectScopes();
        return usedScopeKeys.contains(key);
    }

    /** @return the slots the effect reads, in ascending order */
    public Array<Integer> getUsedScopeKeys() {
        Array<Integer> keys = new Array<>();
        if (effect == null) return keys;
        inspectScopes();
        for (IntSet.IntSetIterator it = usedScopeKeys.iterator(); it.hasNext; ) keys.add(it.next());
        keys.sort();
        return keys;
    }

    /** @return what the slot is used as by this effect, unknown when it cannot be told */
    public ScopeKind getScopeKind(int key) {
        if (effect == null) return ScopeKind.UNKNOWN;
        inspectScopes();
        return scopeKinds.get(key, ScopeKind.UNKNOWN);
    }

    /**
     * Finds the slots the effect's global scope modules declare, and what reads each of them. Done
     * once per effect: the graph does not change while the effect is in use.
     */
    private void inspectScopes() {
        if (usedScopeKeys != null) return;
        usedScopeKeys = new IntSet();
        scopeKinds = new IntMap<>();

        for (IEmitter emitter : effect.getEmitters()) {
            Array<AbstractModule> modules = emitter.getEmitterGraph().getModules();
            for (AbstractModule module : modules) {
                if (module instanceof GlobalScopeModule) usedScopeKeys.add(((GlobalScopeModule) module).getKey());
            }

            // A slot declares no shape: the inputs its value feeds do. Inputs remember what feeds them.
            for (AbstractModule module : modules) {
                for (int input = 0; input < MAX_INPUT_SLOTS; input++) {
                    Slot slot = module.getInputSlot(input);
                    if (slot == null || !(slot.getTargetModule() instanceof GlobalScopeModule)) continue;

                    int key = ((GlobalScopeModule) slot.getTargetModule()).getKey();
                    scopeKinds.put(key, combine(scopeKinds.get(key), classify(module, input)));
                }
            }
        }
    }

    /**
     * The inputs whose meaning is known, each backed by a typed getter of the module. Everything
     * else accepts any shape, or is not confirmed, and says nothing.
     */
    private static ScopeKind classify(AbstractModule module, int input) {
        if (module instanceof ParticleModule) {
            if (input == ParticleModule.COLOR) return ScopeKind.COLOR;
            if (input == ParticleModule.OFFSET || input == ParticleModule.POSITION || input == ParticleModule.TARGET
                    || input == ParticleModule.PIVOT || input == ParticleModule.SIZE) return ScopeKind.VECTOR;
            if (input == ParticleModule.LIFE || input == ParticleModule.TRANSPARENCY || input == ParticleModule.ANGLE
                    || input == ParticleModule.VELOCITY || input == ParticleModule.ROTATION) return ScopeKind.NUMBER;
            return ScopeKind.UNKNOWN;
        }
        if (module instanceof EmitterModule) {
            if (input == EmitterModule.DELAY || input == EmitterModule.DURATION || input == EmitterModule.RATE) return ScopeKind.NUMBER;
            return ScopeKind.UNKNOWN;
        }
        if (module instanceof ColorModule) {
            if (input == ColorModule.R || input == ColorModule.G || input == ColorModule.B) return ScopeKind.NUMBER;
            return ScopeKind.UNKNOWN;
        }
        if (module instanceof GradientColorModule && input == GradientColorModule.ALPHA) return ScopeKind.NUMBER;
        if (module instanceof OffsetModule && input == OffsetModule.ALPHA) return ScopeKind.NUMBER;
        return ScopeKind.UNKNOWN;
    }

    /**
     * A slot read in two places: a number and a vector are both covered by the vector, but a colour
     * read as anything else is ambiguous, and so is anything read by a module accepting any shape.
     */
    private static ScopeKind combine(ScopeKind seen, ScopeKind read) {
        if (seen == null || seen == read) return read;
        if (seen == ScopeKind.UNKNOWN || read == ScopeKind.UNKNOWN) return ScopeKind.UNKNOWN;
        if (seen == ScopeKind.COLOR || read == ScopeKind.COLOR) return ScopeKind.UNKNOWN;
        return ScopeKind.VECTOR;
    }

    public String particleName = "";
    public boolean transform = true;
    public boolean autoStart = true;

    @Override
    public void reset() {
        scopeValues.clear();
        emitting = true;
        usedScopeKeys = null;
        scopeKinds = null;
        if (effect instanceof ParticleEffectInstancePool.PooledParticleEffectInstance) {
            ((ParticleEffectInstancePool.PooledParticleEffectInstance) effect).free();
        }
        effect = null;
        particleName = "";
        transform = true;
        autoStart = true;
    }
}
