package games.rednblack.h2d.extension.talos;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import games.rednblack.editor.renderer.components.particle.TalosDataComponent;

public class TalosSystem extends IteratingSystem {
    private final ComponentMapper<TalosComponent> particleComponentMapper = ComponentMapper.getFor(TalosComponent.class);

    public TalosSystem() {
        super(Family.all(TalosComponent.class, TalosDataComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TalosComponent talosComponent = particleComponentMapper.get(entity);

        talosComponent.effect.update(deltaTime);
    }
}

