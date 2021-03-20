package games.rednblack.h2d.extension.talos;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.World;
import games.rednblack.editor.renderer.box2dLight.RayHandler;
import games.rednblack.editor.renderer.commons.IExternalItemType;
import games.rednblack.editor.renderer.components.particle.TalosDataComponent;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.factory.component.ComponentFactory;
import games.rednblack.editor.renderer.resources.IResourceRetriever;
import games.rednblack.editor.renderer.systems.render.logic.Drawable;
import games.rednblack.editor.renderer.utils.ComponentRetriever;

public class TalosItemType implements IExternalItemType {

    private ComponentFactory factory;
    private IteratingSystem system;
    private Drawable drawable;

    public TalosItemType() {
        factory = new TalosComponentFactory();
        system = new TalosSystem();
        drawable = new TalosDrawableLogic();
    }
    @Override
    public int getTypeId() {
        return EntityFactory.TALOS_TYPE;
    }

    @Override
    public Drawable getDrawable() {
        return drawable;
    }

    @Override
    public EntitySystem getSystem() {
        return system;
    }

    @Override
    public ComponentFactory getComponentFactory() {
        return factory;
    }

    @Override
    public void injectMappers() {
        ComponentRetriever.addMapper(TalosDataComponent.class);
        ComponentRetriever.addMapper(TalosComponent.class);
    }

    @Override
    public void injectDependencies(PooledEngine engine, RayHandler rayHandler, World world, IResourceRetriever rm) {
        factory.injectDependencies(engine, rayHandler, world, rm);
    }
}
