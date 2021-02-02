package games.rednblack.h2d.extension.talos;

import box2dLight.RayHandler;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.physics.box2d.World;
import games.rednblack.editor.renderer.commons.IExternalItemType;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.factory.component.ComponentFactory;
import games.rednblack.editor.renderer.resources.IResourceRetriever;
import games.rednblack.editor.renderer.systems.render.logic.Drawable;

public class TalosItemType implements IExternalItemType {
    @Override
    public int getTypeId() {
        return EntityFactory.TALOS_TYPE;
    }

    @Override
    public Drawable getDrawable() {
        return null;
    }

    @Override
    public EntitySystem getSystem() {
        return null;
    }

    @Override
    public ComponentFactory getComponentFactory() {
        return null;
    }

    @Override
    public void injectMappers() {

    }

    @Override
    public void injectDependencies(PooledEngine engine, RayHandler rayHandler, World world, IResourceRetriever rm) {

    }
}
