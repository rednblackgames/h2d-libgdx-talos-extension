package games.rednblack.h2d.extension.talos;

import com.badlogic.gdx.Gdx;
import games.rednblack.editor.renderer.ecs.ComponentMapper;
import games.rednblack.editor.renderer.ecs.Engine;
import games.rednblack.editor.renderer.systems.WidgetStateSystem;
import com.badlogic.gdx.graphics.Color;
import games.rednblack.editor.renderer.widget.ColorPreviewHandler;
import games.rednblack.editor.renderer.widget.InterpolableOverrideHandler;
import games.rednblack.editor.renderer.widget.ToggleOverrideHandler;
import games.rednblack.talos.runtime.IEmitter;

/**
 * What a widget state may say about a particle effect: whether its emitters are making particles,
 * and what sits in the global scope slots the effect reads.
 *
 * A slot is whatever the artist wired a global scope module to inside the effect, so a state can
 * reach straight into how the effect looks or behaves. Slots hold a number, which means they travel
 * to their value like any other number: a state can fade one over a duration with an easing
 * function. Slots already driven by an anchor constraint are left to it.
 */
public final class TalosStateOverrides {
    private TalosStateOverrides() {
    }

    public static final String EMITTING = "talosEmitting";
    /** A slot property is this plus the slot number, since which slots exist is up to the effect. */
    public static final String SCOPE_PREFIX = "talosScope";

    public static void registerAll(WidgetStateSystem system) {
        system.registerHandler(EMITTING, new TalosEmitting());
    }

    public static String scopeKeyOf(int key) {
        return SCOPE_PREFIX + key;
    }

    /**
     * Makes sure every slot this effect reads has a property of its own. Called as an effect is
     * built, since the slots a project uses are only known from the effects themselves.
     */
    public static void registerScopeHandlers(Engine engine, TalosComponent component) {
        WidgetStateSystem system = engine.getSystem(WidgetStateSystem.class);
        if (system == null) return;

        for (int key : component.getUsedScopeKeys()) {
            String property = scopeKeyOf(key);
            if (system.getHandler(property) == null) system.registerHandler(property, new TalosScope(key));
        }
    }

    /**
     * Stopping lets the particles already on screen live out their lives rather than freezing them
     * where they are, and starting again restarts the effect, so a burst fires anew every time a
     * state begins.
     */
    public static class TalosEmitting implements ToggleOverrideHandler {
        protected ComponentMapper<TalosComponent> talosCM;

        @Override
        public boolean supports(int entity) {
            TalosComponent component = talosCM.get(entity);
            return component != null && component.effect != null;
        }

        @Override
        public String capture(int entity) {
            return Boolean.toString(talosCM.get(entity).emitting);
        }

        @Override
        public void apply(int entity, String value) {
            TalosComponent component = talosCM.get(entity);
            boolean emitting = Boolean.parseBoolean(value);
            if (emitting == component.emitting) return;

            component.emitting = emitting;
            if (emitting) {
                component.effect.restart();
            } else {
                for (IEmitter emitter : component.effect.getEmitters()) emitter.stop();
            }
        }
    }

    /**
     * One global scope slot of the effect. A slot holds up to four numbers, written as a list, so a
     * single figure, a position and a colour are all the same property, and all four travel together
     * when the slot is animated.
     */
    public static class TalosScope implements InterpolableOverrideHandler, ColorPreviewHandler {
        protected ComponentMapper<TalosComponent> talosCM;
        protected ComponentMapper<TalosAnchorConstraintComponent> anchorCM;

        private final int key;
        private final float[] tmp = new float[TalosComponent.ScopeValue.CHANNELS];

        public TalosScope(int key) {
            this.key = key;
        }

        @Override
        public boolean supports(int entity) {
            TalosComponent component = talosCM.get(entity);
            if (component == null || component.effect == null || !component.usesScopeKey(key)) return false;

            // a slot an anchor constraint drives belongs to the layout, not to the states
            return !isAnchored(entity);
        }

        private boolean isAnchored(int entity) {
            TalosAnchorConstraintComponent anchor = anchorCM.get(entity);
            if (anchor == null) return false;

            for (TalosAnchorConstraintComponent.AnchorBinding binding : anchor.bindings) {
                if (binding.scopeKey == key) return true;
            }
            return false;
        }

        @Override
        public String capture(int entity) {
            talosCM.get(entity).getScopeValue(key, tmp);
            return format(tmp);
        }

        @Override
        public void apply(int entity, String value) {
            if (!parseChannels(value, tmp)) {
                Gdx.app.error("WidgetState", "invalid talos scope override: " + value);
                return;
            }
            set(entity, tmp);
        }

        /** Only a slot the effect reads as a colour is shown as one. */
        @Override
        public boolean toColor(int entity, String value, Color out) {
            TalosComponent component = talosCM.get(entity);
            if (component == null || component.getScopeKind(key) != TalosComponent.ScopeKind.COLOR) return false;
            if (!parseChannels(value, tmp)) return false;

            out.set(tmp[0], tmp[1], tmp[2], tmp[3]);
            return true;
        }

        @Override
        public int getChannelCount() {
            return TalosComponent.ScopeValue.CHANNELS;
        }

        @Override
        public void captureChannels(int entity, float[] out) {
            talosCM.get(entity).getScopeValue(key, out);
        }

        @Override
        public boolean parseChannels(String value, float[] out) {
            if (value == null) return false;

            String[] numbers = value.split(",");
            try {
                for (int i = 0; i < TalosComponent.ScopeValue.CHANNELS; i++) {
                    // a slot written with fewer numbers than it holds leaves the rest at zero
                    out[i] = i < numbers.length ? Float.parseFloat(numbers[i].trim()) : 0;
                }
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }

        @Override
        public void applyChannels(int entity, float[] values) {
            set(entity, values);
        }

        private void set(int entity, float[] value) {
            TalosComponent component = talosCM.get(entity);
            component.setScopeValue(key, value);
            component.pushScopeValue(key, value);
        }

        /** The four numbers as a list, with the trailing zeroes left off to keep it readable. */
        public static String format(float[] value) {
            int last = value.length - 1;
            while (last > 0 && value[last] == 0) last--;

            StringBuilder text = new StringBuilder();
            for (int i = 0; i <= last; i++) {
                if (i > 0) text.append(',');
                text.append(value[i]);
            }
            return text.toString();
        }
    }
}
