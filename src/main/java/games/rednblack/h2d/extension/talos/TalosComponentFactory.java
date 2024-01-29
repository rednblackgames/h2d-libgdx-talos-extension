package games.rednblack.h2d.extension.talos;

import com.artemis.ComponentMapper;
import com.artemis.EntityTransmuter;
import com.artemis.EntityTransmuterFactory;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;
import games.rednblack.editor.renderer.box2dLight.RayHandler;
import games.rednblack.editor.renderer.components.BoundingBoxComponent;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.data.MainItemVO;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.renderer.factory.component.ComponentFactory;
import games.rednblack.editor.renderer.resources.IResourceRetriever;
import games.rednblack.talos.runtime.ParticleEffectInstancePool;

public class TalosComponentFactory extends ComponentFactory {

    protected ComponentMapper<TalosComponent> talosCM;

    private EntityTransmuter transmuter;

    public TalosComponentFactory() {
        super();
    }

    @Override
    public void injectDependencies(com.artemis.World engine, RayHandler rayHandler, World world, IResourceRetriever rm) {
        super.injectDependencies(engine, rayHandler, world, rm);

        transmuter = new EntityTransmuterFactory(engine)
                .add(TalosComponent.class)
                .remove(BoundingBoxComponent.class)
                .build();
    }

    @Override
    public void transmuteEntity(int entity) {
        transmuter.transmute(entity);
    }

    @Override
    public int getEntityType() {
        return TalosItemType.TALOS_TYPE;
    }

    @Override
    public void setInitialData(int entity, Object data) {
        talosCM.get(entity).particleName = (String) data;
    }

    @Override
    public Class<TalosVO> getVOType() {
        return TalosVO.class;
    }

    @Override
    public void initializeSpecialComponentsFromVO(int entity, MainItemVO voG) {
        TalosVO vo = (TalosVO) voG;
        TalosComponent talosComponent = talosCM.get(entity);
        talosComponent.particleName = vo.particleName;
        talosComponent.transform = vo.transform;
    }

    @Override
    protected void initializeTransientComponents(int entity) {
        super.initializeTransientComponents(entity);

        TalosComponent component = talosCM.get(entity);
        ParticleEffectInstancePool particleEffectInstancePool = (ParticleEffectInstancePool) rm.getExternalItemType(getEntityType(), component.particleName);
        component.effect = particleEffectInstancePool.obtain();
    }

    @Override
    protected void initializeDimensionsComponent(int entity) {
        DimensionsComponent component = dimensionsCM.get(entity);
        ProjectInfoVO projectInfoVO = rm.getProjectVO();
        float boundBoxSize = 70f;
        component.boundBox = new Rectangle((-boundBoxSize / 2f) / projectInfoVO.pixelToWorld, (-boundBoxSize / 2f) / projectInfoVO.pixelToWorld, boundBoxSize / projectInfoVO.pixelToWorld, boundBoxSize / projectInfoVO.pixelToWorld);
        component.width = boundBoxSize / projectInfoVO.pixelToWorld;
        component.height = boundBoxSize / projectInfoVO.pixelToWorld;
    }
}
