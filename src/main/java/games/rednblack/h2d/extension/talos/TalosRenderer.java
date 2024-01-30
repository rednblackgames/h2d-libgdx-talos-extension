package games.rednblack.h2d.extension.talos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import games.rednblack.talos.runtime.IEmitter;
import games.rednblack.talos.runtime.Particle;
import games.rednblack.talos.runtime.ParticleEffectInstance;
import games.rednblack.talos.runtime.render.ParticleRenderer;

public class TalosRenderer implements ParticleRenderer {
    private final Color entityColor = new Color();
    private Batch batch;

    Color color = new Color(Color.WHITE);
    private final Color tmpColor = new Color();

    public TalosRenderer () {
    }

    @Override
    public void setBatch (Batch batch) {
        this.batch = batch;
    }

    @Override
    public Batch getBatch() {
        return batch;
    }

    public void setEntityColor(Color color, float parentAlpha) {
        entityColor.set(color);
        entityColor.a *= parentAlpha;
    }

    @Override
    public void render (ParticleEffectInstance particleEffectInstance) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        for (int i = 0; i < particleEffectInstance.getEmitters().size; i++) {
            final IEmitter particleEmitter = particleEffectInstance.getEmitters().get(i);
            if(!particleEmitter.isVisible()) continue;
            if(particleEmitter.isBlendAdd()) {
                batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
            } else {
                if (particleEmitter.isAdditive()) {
                    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                } else {
                    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                }
            }

            for (int j = 0; j < particleEmitter.getActiveParticleCount(); j++) {
                renderParticle(batch, particleEmitter.getActiveParticles().get(j), particleEffectInstance.alpha * entityColor.a);
            }
        }

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void renderParticle (Batch batch, Particle particle, float parentAlpha) {
        tmpColor.set(batch.getColor());

        color.set(particle.color);
        color.mul(entityColor);
        color.mul(particle.getEmitter().getTint());
        color.a = particle.transparency * parentAlpha;
        batch.setColor(color);

        if (particle.drawable != null) {
            particle.drawable.setCurrentParticle(particle);
            particle.drawable.draw(batch, particle, color);
        }

        batch.setColor(tmpColor);
    }
}
