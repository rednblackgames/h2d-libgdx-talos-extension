package games.rednblack.h2d.extension.talos;

import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.components.LayoutComponent;
import games.rednblack.editor.renderer.ecs.PooledComponent;
import games.rednblack.editor.renderer.ecs.annotations.EntityId;

public class TalosAnchorConstraintComponent extends PooledComponent {
    public Array<AnchorBinding> bindings = new Array<>();
    public transient int checksum;

    public static class AnchorBinding {
        public int scopeKey;
        public ConstraintData left;
        public ConstraintData right;
        public ConstraintData top;
        public ConstraintData bottom;
        public float horizontalBias = 0.5f;
        public float verticalBias = 0.5f;
    }

    public static class ConstraintData {
        @EntityId public int targetEntity = -1; // -1 = parent
        public LayoutComponent.ConstraintSide targetSide;
        public float margin = 0f;
        public transient String targetUniqueId = null;
        public transient boolean resolved = false;
    }

    @Override
    public void reset() {
        bindings.clear();
        checksum = 0;
    }
}
