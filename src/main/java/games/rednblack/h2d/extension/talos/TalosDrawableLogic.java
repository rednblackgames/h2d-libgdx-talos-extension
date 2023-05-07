package games.rednblack.h2d.extension.talos;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.talosvfx.talos.runtime.IEmitter;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticleEffectInstance;
import com.talosvfx.talos.runtime.render.ParticleRenderer;
import games.rednblack.editor.renderer.components.TintComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.systems.render.logic.DrawableLogic;
import games.rednblack.editor.renderer.utils.TransformMathUtils;

public class TalosDrawableLogic implements DrawableLogic {

    protected ComponentMapper<TalosComponent> talosComponentMapper;
    protected ComponentMapper<TintComponent> tintComponentComponentMapper;
    protected ComponentMapper<TransformComponent> transformComponentMapper;

    protected com.artemis.World engine;

    private final TalosRenderer defaultRenderer = new TalosRenderer();
    private final Color tmpColor = new Color();

    public TalosDrawableLogic() {

    }

    @Override
    public void draw(Batch batch, int entity, float parentAlpha, RenderingType renderingType) {
        tmpColor.set(batch.getColor());

        TintComponent tintComponent = tintComponentComponentMapper.get(entity);

        defaultRenderer.setBatch(batch);
        defaultRenderer.setEntityColor(tintComponent.color, parentAlpha);

        TalosComponent talosComponent = talosComponentMapper.get(entity);
        TransformComponent transformComponent = transformComponentMapper.get(entity);

        if (talosComponent.transform) {
            TransformMathUtils.computeTransform(transformComponent).mulLeft(batch.getTransformMatrix());
            TransformMathUtils.applyTransform(batch, transformComponent);
        } else {
            talosComponent.effect.setPosition(transformComponent.x, transformComponent.y);
        }

        talosComponent.effect.render(defaultRenderer);

        if (talosComponent.transform) {
            TransformMathUtils.resetTransform(batch, transformComponent);
        }

        batch.setColor(tmpColor);
    }

    @Override
    public void beginPipeline() {

    }

    @Override
    public void endPipeline() {

    }

    private static class TalosRenderer implements ParticleRenderer {
        private final Color entityColor = new Color();
        private Batch batch;

        Color color = new Color(Color.WHITE);

        public TalosRenderer () {
        }

        public TalosRenderer (Batch batch) {
            this.batch = batch;
        }

        public void setBatch (Batch batch) {
            this.batch = batch;
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
            color.set(particle.color);
            color.mul(entityColor);
            color.mul(particle.getEmitter().getTint());
            color.a = particle.transparency * parentAlpha;
            batch.setColor(color);

            if (particle.drawable != null) {
                particle.drawable.setCurrentParticle(particle);
                particle.drawable.draw(batch, particle, color);
            }
        }
    }
}