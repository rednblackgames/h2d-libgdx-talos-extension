package games.rednblack.h2d.extension.talos;

import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.ecs.Engine;
import games.rednblack.editor.renderer.data.MainItemVO;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.utils.ComponentRetriever;

public class TalosVO extends MainItemVO {
    public String particleName = "";
    /** Values pushed into the global scope slots of the effect, by slot number. */
    public Array<ScopeValueVO> scopeValues = new Array<>(0);

    public static class ScopeValueVO {
        public int key;
        /** The four numbers of the slot: one on its own, two for a position, four for a colour. */
        public float[] value = new float[TalosComponent.ScopeValue.CHANNELS];

        public ScopeValueVO() {
        }

        public ScopeValueVO(int key, float[] value) {
            this.key = key;
            for (int i = 0; i < this.value.length; i++) this.value[i] = value != null && i < value.length ? value[i] : 0;
        }
    }

    public boolean transform = true;
    public boolean autoStart = true;
    public TalosAnchorConstraintVO anchorConstraints = null;

    public TalosVO() {
        super();
    }

    public TalosVO(TalosVO vo) {
        super(vo);
        particleName = vo.particleName;
        transform = vo.transform;
        autoStart = vo.autoStart;
        for (ScopeValueVO value : vo.scopeValues) scopeValues.add(new ScopeValueVO(value.key, value.value));
        if (vo.anchorConstraints != null)
            anchorConstraints = new TalosAnchorConstraintVO(vo.anchorConstraints);
    }

    @Override
    public void loadFromEntity(int entity, Engine engine, EntityFactory entityFactory) {
        super.loadFromEntity(entity, engine, entityFactory);

        TalosComponent talosComponent = ComponentRetriever.get(entity, TalosComponent.class, engine);
        particleName = talosComponent.particleName;
        transform = talosComponent.transform;
        autoStart = talosComponent.autoStart;

        scopeValues.clear();
        for (TalosComponent.ScopeValue value : talosComponent.scopeValues) {
            scopeValues.add(new ScopeValueVO(value.key, value.value));
        }

        TalosAnchorConstraintComponent anchorComp = ComponentRetriever.get(entity, TalosAnchorConstraintComponent.class, engine);
        if (anchorComp != null && anchorComp.bindings.size > 0) {
            anchorConstraints = new TalosAnchorConstraintVO();
            anchorConstraints.loadFromComponent(anchorComp, engine);
        } else {
            anchorConstraints = null;
        }
    }

    @Override
    public String getResourceName() {
        return particleName;
    }
}
