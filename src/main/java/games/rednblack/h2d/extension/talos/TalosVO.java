package games.rednblack.h2d.extension.talos;

import com.artemis.World;
import games.rednblack.editor.renderer.data.MainItemVO;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.utils.ComponentRetriever;

public class TalosVO extends MainItemVO {
    public String particleName = "";
    public boolean transform = true;

    public TalosVO() {
        super();
    }

    public TalosVO(TalosVO vo) {
        super(vo);
        particleName = vo.particleName;
        transform = vo.transform;
    }

    @Override
    public void loadFromEntity(int entity, World engine, EntityFactory entityFactory) {
        super.loadFromEntity(entity, engine, entityFactory);

        TalosComponent talosComponent = ComponentRetriever.get(entity, TalosComponent.class, engine);
        particleName = talosComponent.particleName;
        transform = talosComponent.transform;
    }

    @Override
    public String getResourceName() {
        return particleName;
    }
}
