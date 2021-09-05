package games.rednblack.h2d.extension.talos;

import com.artemis.systems.IteratingSystem;
import games.rednblack.editor.renderer.commons.IExternalItemType;
import games.rednblack.editor.renderer.components.particle.TalosDataComponent;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.factory.component.ComponentFactory;
import games.rednblack.editor.renderer.systems.render.logic.DrawableLogic;
import games.rednblack.editor.renderer.utils.ComponentRetriever;

public class TalosItemType implements IExternalItemType {

    private ComponentFactory factory;
    private IteratingSystem system;
    private DrawableLogic drawableLogic;

    public TalosItemType() {
        factory = new TalosComponentFactory();
        system = new TalosSystem();
        drawableLogic = new TalosDrawableLogic();
    }
    @Override
    public int getTypeId() {
        return EntityFactory.TALOS_TYPE;
    }

    @Override
    public DrawableLogic getDrawable() {
        return drawableLogic;
    }

    @Override
    public IteratingSystem getSystem() {
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
}
