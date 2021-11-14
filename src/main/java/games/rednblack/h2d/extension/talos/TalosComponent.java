package games.rednblack.h2d.extension.talos;

import com.artemis.PooledComponent;
import com.talosvfx.talos.runtime.ParticleEffectInstance;

public class TalosComponent extends PooledComponent {
    public transient ParticleEffectInstance effect = null;

    @Override
    public void reset() {
        effect = null;
    }
}
