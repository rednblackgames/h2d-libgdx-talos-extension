package games.rednblack.h2d.extension.talos;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.assets.AtlasAssetProvider;
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
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.factory.component.ComponentFactory;
import games.rednblack.editor.renderer.resources.IResourceRetriever;

import java.io.File;

public class TalosComponentFactory extends ComponentFactory {
    AtlasAssetProvider assetProvider;

    public TalosComponentFactory() {
        super();
    }

    public TalosComponentFactory(PooledEngine engine, RayHandler rayHandler, World world, IResourceRetriever rm) {
        super(engine, rayHandler, world, rm);
    }

    @Override
    public void injectDependencies(PooledEngine engine, RayHandler rayHandler, World world, IResourceRetriever rm) {
        super.injectDependencies(engine, rayHandler, world, rm);
    }

    @Override
    public void createComponents(Entity root, Entity entity, MainItemVO vo) {
        createCommonComponents(entity, vo, EntityFactory.TALOS_TYPE);
        entity.remove(BoundingBoxComponent.class);
        createParentNodeComponent(root, entity);
        createNodeComponent(root, entity);
        createDataComponent(entity, (TalosVO) vo);
        createParticleComponent(entity, (TalosVO) vo);
    }

    @Override
    protected DimensionsComponent createDimensionsComponent(Entity entity, MainItemVO vo) {
        DimensionsComponent component = engine.createComponent(DimensionsComponent.class);

        ProjectInfoVO projectInfoVO = rm.getProjectVO();
        float boundBoxSize = 70f;
        component.boundBox = new Rectangle((-boundBoxSize / 2f) / projectInfoVO.pixelToWorld, (-boundBoxSize / 2f) / projectInfoVO.pixelToWorld, boundBoxSize / projectInfoVO.pixelToWorld, boundBoxSize / projectInfoVO.pixelToWorld);
        component.width = boundBoxSize / projectInfoVO.pixelToWorld;
        component.height = boundBoxSize / projectInfoVO.pixelToWorld;

        entity.add(component);
        return component;
    }

    private FileHandle talosToLoad;

    protected TalosComponent createParticleComponent(Entity entity, TalosVO vo) {
        assetProvider = new AtlasAssetProvider(rm.getMainPack());
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

        TalosComponent component = engine.createComponent(TalosComponent.class);
        ParticleEffectDescriptor effectDescriptor = new ParticleEffectDescriptor();
        effectDescriptor.setAssetProvider(assetProvider);
        talosToLoad = rm.getTalosVFX(vo.particleName);
        effectDescriptor.load(talosToLoad);
        component.effect = effectDescriptor.createEffectInstance();

        entity.add(component);

        return component;
    }

    protected TalosDataComponent createDataComponent(Entity entity, TalosVO vo) {
        TalosDataComponent dataComponent = engine.createComponent(TalosDataComponent.class);
        dataComponent.particleName = vo.particleName;
        dataComponent.transform = vo.transform;

        entity.add(dataComponent);

        return dataComponent;
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
}