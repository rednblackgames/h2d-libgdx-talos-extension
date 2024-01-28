package games.rednblack.h2d.extension.talos;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import games.rednblack.talos.runtime.assets.BaseAssetProvider;
import games.rednblack.editor.renderer.resources.IResourceRetriever;

public class ResourceRetrieverAssetProvider extends BaseAssetProvider {
    private final IResourceRetriever resourceRetriever;

    public ResourceRetrieverAssetProvider (final IResourceRetriever resourceRetriever) {
        this.resourceRetriever = resourceRetriever;

        setAssetHandler(TextureRegion.class, new AssetHandler<TextureRegion>() {
            @Override
            public TextureRegion findAsset (String assetName) {
                return resourceRetriever.getTextureRegion(assetName);
            }
        });

        setAssetHandler(Sprite.class, new AssetHandler<Sprite>() {
            @Override
            public Sprite findAsset (String assetName) {
                return newSprite((TextureAtlas.AtlasRegion) resourceRetriever.getTextureRegion(assetName));
            }
        });
    }

    private Sprite newSprite (TextureAtlas.AtlasRegion region) {
        if (region.packedWidth == region.originalWidth && region.packedHeight == region.originalHeight) {
            if (region.rotate) {
                Sprite sprite = new Sprite(region);
                sprite.setBounds(0, 0, region.getRegionHeight(), region.getRegionWidth());
                sprite.rotate90(true);
                return sprite;
            }
            return new Sprite(region);
        }
        return new TextureAtlas.AtlasSprite(region);
    }
}
