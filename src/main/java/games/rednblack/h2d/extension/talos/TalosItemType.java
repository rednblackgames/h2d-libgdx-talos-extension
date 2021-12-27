package games.rednblack.h2d.extension.talos;

import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.assets.BaseAssetProvider;
import com.talosvfx.talos.runtime.utils.ShaderDescriptor;
import com.talosvfx.talos.runtime.utils.VectorField;
import games.rednblack.editor.renderer.commons.IExternalItemType;
import games.rednblack.editor.renderer.factory.component.ComponentFactory;
import games.rednblack.editor.renderer.resources.IResourceRetriever;
import games.rednblack.editor.renderer.systems.render.logic.DrawableLogic;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.renderer.utils.HyperJson;

import java.io.File;
import java.util.HashMap;

public class TalosItemType implements IExternalItemType {
    public static final int TALOS_TYPE = 10;
    public String talosPath = "talos-vfx";

    private ComponentFactory factory;
    private IteratingSystem system;
    private DrawableLogic drawableLogic;
    private ResourceRetrieverAssetProvider assetProvider;
    private FileHandle talosToLoad;

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
    public boolean hasResources() {
        return true;
    }

    @Override
    public void loadExternalTypesAsync(IResourceRetriever rm, ObjectSet<String> assetsToLoad, HashMap<String, Object> assets) {
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

        // empty existing ones that are not scheduled to load
        for (String key : assets.keySet()) {
            if (!assetsToLoad.contains(key)) {
                assets.remove(key);
            }
        }

        for (String name : assetsToLoad) {
            ParticleEffectDescriptor effectDescriptor = new ParticleEffectDescriptor();
            effectDescriptor.setAssetProvider(assetProvider);
            talosToLoad = Gdx.files.internal(formatResourcePath(name));
            effectDescriptor.load(talosToLoad);

            assets.put(name, effectDescriptor);
        }
    }

    @Override
    public void loadExternalTypesSync(IResourceRetriever rm, ObjectSet<String> assetsToLoad, HashMap<String, Object> assets) {

    }

    public String formatResourcePath(String resName) {
        return talosPath + File.separator + resName;
    }
}
