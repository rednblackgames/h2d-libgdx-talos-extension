package games.rednblack.h2d.extension.talos;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import games.rednblack.editor.renderer.components.particle.TalosDataComponent;

@All({TalosComponent.class, TalosDataComponent.class})
public class TalosSystem extends IteratingSystem {
    protected ComponentMapper<TalosComponent> particleComponentMapper;

    @Override
    protected void process(int entity) {
        TalosComponent talosComponent = particleComponentMapper.get(entity);

        talosComponent.effect.update(world.getDelta());
    }
}

