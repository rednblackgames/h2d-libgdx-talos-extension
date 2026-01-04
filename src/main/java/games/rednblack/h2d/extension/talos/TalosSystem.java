package games.rednblack.h2d.extension.talos;

import games.rednblack.editor.renderer.ecs.ComponentMapper;
import games.rednblack.editor.renderer.ecs.annotations.All;
import games.rednblack.editor.renderer.ecs.systems.IteratingSystem;

@All({TalosComponent.class})
public class TalosSystem extends IteratingSystem {
    protected ComponentMapper<TalosComponent> particleComponentMapper;

    @Override
    protected void process(int entity) {
        TalosComponent talosComponent = particleComponentMapper.get(entity);

        talosComponent.effect.update(engine.getDelta());
    }
}

