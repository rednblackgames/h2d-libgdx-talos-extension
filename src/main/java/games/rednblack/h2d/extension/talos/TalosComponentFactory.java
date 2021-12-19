package games.rednblack.h2d.extension.talos;

import com.artemis.ComponentMapper;
import com.artemis.EntityTransmuter;
import com.artemis.EntityTransmuterFactory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.assets.BaseAssetProvider;
import com.talosvfx.talos.runtime.utils.ShaderDescriptor;
import com.talosvfx.talos.runtime.utils.VectorField;
import games.rednblack.editor.renderer.box2dLight.RayHandler;
import games.rednblack.editor.renderer.components.BoundingBoxComponent;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.particle.TalosDataComponent;
import games.rednblack.editor.renderer.data.MainItemVO;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.renderer.data.TalosVO;
import games.rednblack.editor.renderer.factory.component.ComponentFactory;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.resources.IResourceRetriever;

import java.io.File;

public class TalosComponentFactory extends ComponentFactory {

    protected ComponentMapper<TalosComponent> talosCM;
    protected ComponentMapper<TalosDataComponent> talosDataCM;

    private FileHandle talosToLoad;
    private EntityTransmuter transmuter;

    private ResourceRetrieverAssetProvider assetProvider;

    public TalosComponentFactory() {
        super();
    }

    @Override
    public void injectDependencies(com.artemis.World engine, RayHandler rayHandler, World world, IResourceRetriever rm) {
        super.injectDependencies(engine, rayHandler, world, rm);

        transmuter = new EntityTransmuterFactory(engine)
                .add(TalosComponent.class)
                .add(TalosDataComponent.class)
                .remove(BoundingBoxComponent.class)
                .build();

        assetProvider = new ResourceRetrieverAssetProvider(rm);
        assetProvider.setAssetHandler(ShaderDescriptor.class, new BaseAssetProvider.AssetHandler<ShaderDescriptor>() {
            @Override
            public ShaderDescriptor findAsset(String assetName) {
                return findShaderDescriptorOnLoad(assetName);
            }
        });
        assetProvider.setAssetHandler(VectorField.class, new BaseAssetProvider.AssetHandler<VectorField>() {
            @Override
            public VectorField findAsset(String assetName) {
                return findVectorFieldDescriptorOnLoad(assetName);
            }
        });
    }

    @Override
    public void transmuteEntity(int entity) {
        transmuter.transmute(entity);
    }

    @Override
    public int getEntityType() {
        return EntityFactory.TALOS_TYPE;
    }

    @Override
    public void setInitialData(int entity, Object data) {
        talosDataCM.get(entity).particleName = (String) data;
    }

    @Override
    public Class<TalosVO> getVOType() {
        return TalosVO.class;
    }

    @Override
    public void initializeSpecialComponentsFromVO(int entity, MainItemVO voG) {
        TalosVO vo = (TalosVO) voG;
        TalosDataComponent talosDataComponent = talosDataCM.get(entity);
        talosDataComponent.particleName = vo.particleName;
        talosDataComponent.transform = vo.transform;
    }

    @Override
    protected void initializeTransientComponents(int entity) {
        super.initializeTransientComponents(entity);

        TalosDataComponent data = talosDataCM.get(entity);
        TalosComponent component = talosCM.get(entity);
        ParticleEffectDescriptor effectDescriptor = new ParticleEffectDescriptor();
        effectDescriptor.setAssetProvider(assetProvider);
        talosToLoad = rm.getTalosVFX(data.particleName);
        effectDescriptor.load(talosToLoad);
        component.effect = effectDescriptor.createEffectInstance();
    }

    private ObjectMap<String, ShaderDescriptor> shaderDescriptorObjectMap = new ObjectMap<>();
    private ShaderDescriptor findShaderDescriptorOnLoad (String assetName) {
        ShaderDescriptor asset = shaderDescriptorObjectMap.get(assetName);
        if (asset == null) {
            String path = talosToLoad.parent().path() + File.separator + assetName;
            final FileHandle file = Gdx.files.internal(path);

            asset = new ShaderDescriptor();
            if (file.exists()) {
                asset.setData(file.readString());
            }
        }
        return asset;
    }

    private ObjectMap<String, VectorField> vectorFieldDescriptorObjectMap = new ObjectMap<>();
    private VectorField findVectorFieldDescriptorOnLoad (String assetName) {
        VectorField asset = vectorFieldDescriptorObjectMap.get(assetName);
        if (asset == null) {
            final FileHandle file = new FileHandle(talosToLoad.parent().path() + File.separator + assetName + ".fga");

            asset = new VectorField();
            if (file.exists()) {
                asset.setBakedData(file);
            }
        }
        return asset;
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
