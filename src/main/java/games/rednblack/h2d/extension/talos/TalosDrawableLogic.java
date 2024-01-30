package games.rednblack.h2d.extension.talos;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import games.rednblack.editor.renderer.components.TintComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.systems.render.logic.DrawableLogic;
import games.rednblack.editor.renderer.utils.TransformMathUtils;

public class TalosDrawableLogic implements DrawableLogic {

    protected ComponentMapper<TalosComponent> talosComponentMapper;
    protected ComponentMapper<TintComponent> tintComponentComponentMapper;
    protected ComponentMapper<TransformComponent> transformComponentMapper;

    private final TalosRenderer defaultRenderer = new TalosRenderer();
    private final Color tmpColor = new Color();

    public TalosDrawableLogic() {

    }

    @Override
    public void draw(Batch batch, int entity, float parentAlpha, RenderingType renderingType) {
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
    }

    @Override
    public void beginPipeline() {

    }

    @Override
    public void endPipeline() {

    }

}