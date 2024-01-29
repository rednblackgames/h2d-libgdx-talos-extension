package games.rednblack.h2d.extension.talos;

import com.artemis.PooledComponent;
import games.rednblack.talos.runtime.ParticleEffectInstance;
import games.rednblack.talos.runtime.ParticleEffectInstancePool;

public class TalosComponent extends PooledComponent {
    public transient ParticleEffectInstance effect = null;

    public String particleName = "";
    public boolean transform = true;

    @Override
    public void reset() {
        if (effect instanceof ParticleEffectInstancePool.PooledParticleEffectInstance) {
            ((ParticleEffectInstancePool.PooledParticleEffectInstance) effect).free();
        }
        effect = null;
        particleName = "";
        transform = true;
    }
}
