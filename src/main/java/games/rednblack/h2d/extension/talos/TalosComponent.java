package games.rednblack.h2d.extension.talos;

import com.talosvfx.talos.runtime.ParticleEffectInstance;
import games.rednblack.editor.renderer.components.BaseComponent;

public class TalosComponent implements BaseComponent {
    public ParticleEffectInstance effect = null;

    @Override
    public void reset() {
        effect = null;
    }
}
