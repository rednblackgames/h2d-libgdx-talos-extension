package games.rednblack.h2d.extension.talos;

import games.rednblack.editor.renderer.ecs.ComponentMapper;
import games.rednblack.editor.renderer.ecs.Engine;
import games.rednblack.editor.renderer.ecs.EntityTransmuter;
import games.rednblack.editor.renderer.ecs.EntityTransmuterFactory;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;
import games.rednblack.editor.renderer.lights.RayHandler;
import games.rednblack.editor.renderer.components.BoundingBoxComponent;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.data.MainItemVO;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.renderer.factory.component.ComponentFactory;
import games.rednblack.editor.renderer.resources.IResourceRetriever;
import games.rednblack.talos.runtime.ParticleEffectInstancePool;

public class TalosComponentFactory extends ComponentFactory {

    protected ComponentMapper<TalosComponent> talosCM;
    protected ComponentMapper<TalosAnchorConstraintComponent> anchorCM;

    private EntityTransmuter transmuter;

    public TalosComponentFactory() {
        super();
    }

    @Override
    public void injectDependencies(Engine engine, RayHandler rayHandler, World world, IResourceRetriever rm) {
        super.injectDependencies(engine, rayHandler, world, rm);

        transmuter = new EntityTransmuterFactory(engine)
                .add(TalosComponent.class)
                .add(TalosAnchorConstraintComponent.class)
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
        talosComponent.autoStart = vo.autoStart;

        talosComponent.scopeValues.clear();
        for (TalosVO.ScopeValueVO value : vo.scopeValues) {
            talosComponent.scopeValues.add(new TalosComponent.ScopeValue(value.key, value.value));
        }

        if (vo.anchorConstraints != null && vo.anchorConstraints.bindings != null) {
            TalosAnchorConstraintComponent anchorComp = anchorCM.get(entity);
            for (TalosAnchorConstraintVO.AnchorBindingVO bvo : vo.anchorConstraints.bindings) {
                TalosAnchorConstraintComponent.AnchorBinding ab = new TalosAnchorConstraintComponent.AnchorBinding();
                ab.scopeKey = bvo.scopeKey;
                ab.horizontalBias = bvo.horizontalBias;
                ab.verticalBias = bvo.verticalBias;
                ab.left = createConstraintData(bvo.left);
                ab.right = createConstraintData(bvo.right);
                ab.top = createConstraintData(bvo.top);
                ab.bottom = createConstraintData(bvo.bottom);
                anchorComp.bindings.add(ab);
            }
        }
    }

    @Override
    protected void initializeTransientComponents(int entity) {
        super.initializeTransientComponents(entity);

        TalosComponent component = talosCM.get(entity);
        ParticleEffectInstancePool particleEffectInstancePool = (ParticleEffectInstancePool) rm.getExternalItemType(getEntityType(), component.particleName);
        component.effect = particleEffectInstancePool.obtain();
        // the instance comes from a pool, so whatever the scene says the slots hold has to be pushed in
        component.applyScopeValues();
        if (!component.autoStart)
            component.effect.pause();

        // a state may reach into any slot the effect reads, and only the effect knows which those are
        TalosStateOverrides.registerScopeHandlers(engine, component);
    }

    private TalosAnchorConstraintComponent.ConstraintData createConstraintData(TalosAnchorConstraintVO.ConstraintDataVO dataVO) {
        if (dataVO == null) return null;
        TalosAnchorConstraintComponent.ConstraintData data = new TalosAnchorConstraintComponent.ConstraintData();
        data.targetSide = dataVO.targetSide;
        data.margin = dataVO.margin;
        if (dataVO.targetUniqueId == null) {
            data.targetEntity = -1;
            data.resolved = true;
        } else {
            data.targetUniqueId = dataVO.targetUniqueId;
            data.resolved = false;
        }
        return data;
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
