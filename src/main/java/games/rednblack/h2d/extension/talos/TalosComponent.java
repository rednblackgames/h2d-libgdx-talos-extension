package games.rednblack.h2d.extension.talos;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntSet;
import games.rednblack.talos.runtime.IEmitter;
import games.rednblack.talos.runtime.modules.AbstractModule;
import games.rednblack.talos.runtime.modules.GlobalScopeModule;
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
        if (usedScopeKeys == null) {
            usedScopeKeys = new IntSet();
            for (IEmitter emitter : effect.getEmitters()) {
                for (AbstractModule module : emitter.getEmitterGraph().getModules()) {
                    if (module instanceof GlobalScopeModule) usedScopeKeys.add(((GlobalScopeModule) module).getKey());
                }
            }
        }
        return usedScopeKeys.contains(key);
    }

    /** @return the slots the effect reads, in ascending order */
    public Array<Integer> getUsedScopeKeys() {
        Array<Integer> keys = new Array<>();
        if (effect == null) return keys;
        usesScopeKey(0);
        for (IntSet.IntSetIterator it = usedScopeKeys.iterator(); it.hasNext; ) keys.add(it.next());
        keys.sort();
        return keys;
    }

    public String particleName = "";
    public boolean transform = true;
    public boolean autoStart = true;

    @Override
    public void reset() {
        scopeValues.clear();
        emitting = true;
        usedScopeKeys = null;
        if (effect instanceof ParticleEffectInstancePool.PooledParticleEffectInstance) {
            ((ParticleEffectInstancePool.PooledParticleEffectInstance) effect).free();
        }
        effect = null;
        particleName = "";
        transform = true;
        autoStart = true;
    }
}
