package games.rednblack.h2d.extension.talos;

import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.components.LayoutComponent;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.ecs.Engine;
import games.rednblack.editor.renderer.utils.ComponentRetriever;

import java.util.Objects;

public class TalosAnchorConstraintVO {
    public Array<AnchorBindingVO> bindings = null;

    public static class AnchorBindingVO {
        public int scopeKey;
        public ConstraintDataVO left;
        public ConstraintDataVO right;
        public ConstraintDataVO top;
        public ConstraintDataVO bottom;
        public float horizontalBias = 0.5f;
        public float verticalBias = 0.5f;

        public AnchorBindingVO() {
        }

        public AnchorBindingVO(AnchorBindingVO vo) {
            if (vo == null) return;
            scopeKey = vo.scopeKey;
            if (vo.left != null) left = new ConstraintDataVO(vo.left);
            if (vo.right != null) right = new ConstraintDataVO(vo.right);
            if (vo.top != null) top = new ConstraintDataVO(vo.top);
            if (vo.bottom != null) bottom = new ConstraintDataVO(vo.bottom);
            horizontalBias = vo.horizontalBias;
            verticalBias = vo.verticalBias;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AnchorBindingVO that = (AnchorBindingVO) o;
            return scopeKey == that.scopeKey &&
                    Float.compare(that.horizontalBias, horizontalBias) == 0 &&
                    Float.compare(that.verticalBias, verticalBias) == 0 &&
                    Objects.equals(left, that.left) &&
                    Objects.equals(right, that.right) &&
                    Objects.equals(top, that.top) &&
                    Objects.equals(bottom, that.bottom);
        }

        @Override
        public int hashCode() {
            return Objects.hash(scopeKey, left, right, top, bottom, horizontalBias, verticalBias);
        }
    }

    public static class ConstraintDataVO {
        public String targetUniqueId = null; // null = parent
        public LayoutComponent.ConstraintSide targetSide;
        public float margin = 0f;

        public ConstraintDataVO() {
        }

        public ConstraintDataVO(ConstraintDataVO vo) {
            if (vo == null) return;
            targetUniqueId = vo.targetUniqueId;
            targetSide = vo.targetSide;
            margin = vo.margin;
        }

        public void loadFromComponent(TalosAnchorConstraintComponent.ConstraintData data, Engine engine) {
            if (data.targetEntity == -1) {
                targetUniqueId = null;
            } else {
                MainItemComponent mic = ComponentRetriever.get(data.targetEntity, MainItemComponent.class, engine);
                targetUniqueId = mic != null ? mic.uniqueId : null;
            }
            targetSide = data.targetSide;
            margin = data.margin;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConstraintDataVO that = (ConstraintDataVO) o;
            return Float.compare(that.margin, margin) == 0 &&
                    Objects.equals(targetUniqueId, that.targetUniqueId) &&
                    targetSide == that.targetSide;
        }

        @Override
        public int hashCode() {
            return Objects.hash(targetUniqueId, targetSide, margin);
        }
    }

    public TalosAnchorConstraintVO() {
    }

    public TalosAnchorConstraintVO(TalosAnchorConstraintVO vo) {
        if (vo == null) return;
        if (vo.bindings != null) {
            bindings = new Array<>();
            for (AnchorBindingVO b : vo.bindings) {
                bindings.add(new AnchorBindingVO(b));
            }
        }
    }

    public void loadFromComponent(TalosAnchorConstraintComponent comp, Engine engine) {
        if (comp.bindings.size == 0) {
            bindings = null;
            return;
        }
        bindings = new Array<>();
        for (TalosAnchorConstraintComponent.AnchorBinding ab : comp.bindings) {
            AnchorBindingVO bvo = new AnchorBindingVO();
            bvo.scopeKey = ab.scopeKey;
            if (ab.left != null) {
                bvo.left = new ConstraintDataVO();
                bvo.left.loadFromComponent(ab.left, engine);
            }
            if (ab.right != null) {
                bvo.right = new ConstraintDataVO();
                bvo.right.loadFromComponent(ab.right, engine);
            }
            if (ab.top != null) {
                bvo.top = new ConstraintDataVO();
                bvo.top.loadFromComponent(ab.top, engine);
            }
            if (ab.bottom != null) {
                bvo.bottom = new ConstraintDataVO();
                bvo.bottom.loadFromComponent(ab.bottom, engine);
            }
            bvo.horizontalBias = ab.horizontalBias;
            bvo.verticalBias = ab.verticalBias;
            bindings.add(bvo);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TalosAnchorConstraintVO that = (TalosAnchorConstraintVO) o;
        if (bindings == null && that.bindings == null) return true;
        if (bindings == null || that.bindings == null) return false;
        if (bindings.size != that.bindings.size) return false;
        for (int i = 0; i < bindings.size; i++) {
            if (!bindings.get(i).equals(that.bindings.get(i))) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (bindings == null) return 0;
        int h = 1;
        for (AnchorBindingVO b : bindings) {
            h = 31 * h + b.hashCode();
        }
        return h;
    }
}
