package games.rednblack.h2d.extension.talos;

import com.artemis.systems.IteratingSystem;
import games.rednblack.editor.renderer.commons.IExternalItemType;
import games.rednblack.editor.renderer.factory.component.ComponentFactory;
import games.rednblack.editor.renderer.systems.render.logic.DrawableLogic;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.renderer.utils.HyperJson;

import java.io.File;

public class TalosItemType implements IExternalItemType {
    public static final int TALOS_TYPE = 10;
    public String talosPath = "talos-vfx";

    private ComponentFactory factory;
    private IteratingSystem system;
    private DrawableLogic drawableLogic;

    public TalosItemType() {
        factory = new TalosComponentFactory();
        system = new TalosSystem();
        drawableLogic = new TalosDrawableLogic();

        HyperJson.getJson().addClassTag(TalosVO.class.getSimpleName(), TalosVO.class);
    }
    @Override
    public int getTypeId() {
        return TALOS_TYPE;
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
        ComponentRetriever.addMapper(TalosComponent.class);
    }

    @Override
    public boolean hasResources() {
        return true;
    }

    @Override
    public String formatResourcePath(String resName) {
        return talosPath + File.separator + resName;
    }
}
