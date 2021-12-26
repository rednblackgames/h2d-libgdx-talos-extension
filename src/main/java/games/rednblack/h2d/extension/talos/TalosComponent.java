package games.rednblack.h2d.extension.talos;

import com.artemis.PooledComponent;
import com.talosvfx.talos.runtime.ParticleEffectInstance;

public class TalosComponent extends PooledComponent {
    public transient ParticleEffectInstance effect = null;

    public String particleName = "";
    public boolean transform = true;

    @Override
    public void reset() {
        effect = null;
        particleName = "";
        transform = true;
    }
}
