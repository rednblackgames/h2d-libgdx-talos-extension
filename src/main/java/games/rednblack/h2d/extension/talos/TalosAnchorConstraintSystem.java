package games.rednblack.h2d.extension.talos;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.components.*;
import games.rednblack.editor.renderer.ecs.ComponentMapper;
import games.rednblack.editor.renderer.ecs.annotations.All;
import games.rednblack.editor.renderer.ecs.systems.IteratingSystem;
import games.rednblack.editor.renderer.utils.TransformMathUtils;

@All({TalosComponent.class, TalosAnchorConstraintComponent.class})
public class TalosAnchorConstraintSystem extends IteratingSystem {

    protected ComponentMapper<TalosComponent> talosMapper;
    protected ComponentMapper<TalosAnchorConstraintComponent> anchorMapper;
    protected ComponentMapper<TransformComponent> transformMapper;
    protected ComponentMapper<DimensionsComponent> dimensionsMapper;
    protected ComponentMapper<ParentNodeComponent> parentNodeMapper;
    protected ComponentMapper<NodeComponent> nodeMapper;
    protected ComponentMapper<MainItemComponent> mainItemMapper;
    protected ComponentMapper<BoundingBoxComponent> boundingBoxMapper;

    private final Vector2 tmpVec = new Vector2();

    @Override
    protected void process(int entity) {
        TalosAnchorConstraintComponent anchorComp = anchorMapper.get(entity);
        TalosComponent talosComp = talosMapper.get(entity);

        if (anchorComp.bindings.size == 0 || talosComp.effect == null) return;

        // Resolve any unresolved bindings
        resolveBindings(anchorComp, entity);

        // Checksum-based skip
        int checksum = calcChecksum(anchorComp, entity);
        if (checksum == anchorComp.checksum) return;

        ParentNodeComponent parentNode = parentNodeMapper.get(entity);
        if (parentNode == null) return;
        int parent = parentNode.parentEntity;
        if (parent == -1) return;

        DimensionsComponent parentDimensions = dimensionsMapper.get(parent);
        if (parentDimensions == null) return;

        for (int i = 0; i < anchorComp.bindings.size; i++) {
            TalosAnchorConstraintComponent.AnchorBinding binding = anchorComp.bindings.get(i);

            float x = resolveHorizontal(binding, parentDimensions);
            float y = resolveVertical(binding, parentDimensions);

            if (Float.isNaN(x) && Float.isNaN(y)) continue;

            // Convert from parent-local space to entity-local space
            tmpVec.set(Float.isNaN(x) ? 0f : x, Float.isNaN(y) ? 0f : y);
            TransformMathUtils.parentToLocalCoordinates(entity, tmpVec, transformMapper);

            talosComp.effect.getScope().setDynamicValue(binding.scopeKey, tmpVec);
        }

        anchorComp.checksum = checksum;
    }

    // ----------------------------------------------------------------
    // Horizontal resolution (left/right + bias) -> X
    // Mirrors LayoutSystem.processHorizontal but outputs a position
    // instead of modifying transform.
    // ----------------------------------------------------------------

    private float resolveHorizontal(TalosAnchorConstraintComponent.AnchorBinding binding,
                                     DimensionsComponent parentDimensions) {
        float leftAnchor = resolveAnchor(binding.left, parentDimensions);
        float rightAnchor = resolveAnchor(binding.right, parentDimensions);

        if (!Float.isNaN(leftAnchor) && !Float.isNaN(rightAnchor)) {
            float leftPos = leftAnchor + binding.left.margin;
            float rightPos = rightAnchor - binding.right.margin;
            return leftPos + (rightPos - leftPos) * binding.horizontalBias;
        } else if (!Float.isNaN(leftAnchor)) {
            return leftAnchor + binding.left.margin;
        } else if (!Float.isNaN(rightAnchor)) {
            return rightAnchor - binding.right.margin;
        }
        return Float.NaN;
    }

    // ----------------------------------------------------------------
    // Vertical resolution (bottom/top + bias) -> Y
    // ----------------------------------------------------------------

    private float resolveVertical(TalosAnchorConstraintComponent.AnchorBinding binding,
                                   DimensionsComponent parentDimensions) {
        float bottomAnchor = resolveAnchor(binding.bottom, parentDimensions);
        float topAnchor = resolveAnchor(binding.top, parentDimensions);

        if (!Float.isNaN(bottomAnchor) && !Float.isNaN(topAnchor)) {
            float bottomPos = bottomAnchor + binding.bottom.margin;
            float topPos = topAnchor - binding.top.margin;
            return bottomPos + (topPos - bottomPos) * binding.verticalBias;
        } else if (!Float.isNaN(bottomAnchor)) {
            return bottomAnchor + binding.bottom.margin;
        } else if (!Float.isNaN(topAnchor)) {
            return topAnchor - binding.top.margin;
        }
        return Float.NaN;
    }

    // ----------------------------------------------------------------
    // Anchor resolution
    // ----------------------------------------------------------------

    private float resolveAnchor(TalosAnchorConstraintComponent.ConstraintData data,
                                 DimensionsComponent parentDimensions) {
        if (data == null || !data.resolved) return Float.NaN;

        if (data.targetEntity == -1) {
            return resolveParentSide(data.targetSide, parentDimensions);
        } else {
            if (!engine.getEntityManager().isActive(data.targetEntity)) return Float.NaN;

            TransformComponent siblingTransform = transformMapper.get(data.targetEntity);
            DimensionsComponent siblingDimensions = dimensionsMapper.get(data.targetEntity);
            if (siblingTransform == null || siblingDimensions == null) return Float.NaN;

            return resolveSiblingSide(data.targetSide, data.targetEntity, siblingTransform, siblingDimensions);
        }
    }

    private float resolveParentSide(LayoutComponent.ConstraintSide side, DimensionsComponent parentDimensions) {
        if (side == null) return Float.NaN;
        switch (side) {
            case LEFT:
            case BOTTOM:
                return 0f;
            case RIGHT:
                return parentDimensions.width;
            case TOP:
                return parentDimensions.height;
            default:
                return Float.NaN;
        }
    }

    private float resolveSiblingSide(LayoutComponent.ConstraintSide side, int siblingEntity,
                                      TransformComponent siblingTransform,
                                      DimensionsComponent siblingDimensions) {
        if (side == null) return Float.NaN;

        MainItemComponent sibMic = mainItemMapper.get(siblingEntity);
        boolean sibVisible = sibMic == null || sibMic.visible;

        float left, bottom, right, top;
        if (!sibVisible) {
            left = 0; bottom = 0; right = 0; top = 0;
        } else {
            BoundingBoxComponent sibBB = boundingBoxMapper.get(siblingEntity);
            if (sibBB != null) {
                left = sibBB.parentLocalAABB.x;
                bottom = sibBB.parentLocalAABB.y;
                right = left + sibBB.parentLocalAABB.width;
                top = bottom + sibBB.parentLocalAABB.height;
            } else {
                left = 0; bottom = 0;
                right = siblingDimensions.width;
                top = siblingDimensions.height;
            }
        }

        switch (side) {
            case LEFT:
                return siblingTransform.x + left;
            case RIGHT:
                return siblingTransform.x + right;
            case BOTTOM:
                return siblingTransform.y + bottom;
            case TOP:
                return siblingTransform.y + top;
            default:
                return Float.NaN;
        }
    }

    // ----------------------------------------------------------------
    // Lazy constraint resolution (uniqueId -> entityId)
    // ----------------------------------------------------------------

    private void resolveBindings(TalosAnchorConstraintComponent anchorComp, int entity) {
        for (int i = 0; i < anchorComp.bindings.size; i++) {
            TalosAnchorConstraintComponent.AnchorBinding binding = anchorComp.bindings.get(i);
            resolveConstraintData(binding.left, entity);
            resolveConstraintData(binding.right, entity);
            resolveConstraintData(binding.top, entity);
            resolveConstraintData(binding.bottom, entity);
        }
    }

    private void resolveConstraintData(TalosAnchorConstraintComponent.ConstraintData data, int entity) {
        if (data == null || data.resolved) return;

        if (data.targetUniqueId == null) {
            data.targetEntity = -1;
            data.resolved = true;
        } else {
            int resolved = findEntityByUniqueId(data.targetUniqueId, entity);
            if (resolved != -1) {
                data.targetEntity = resolved;
                data.resolved = true;
            }
        }
    }

    private int findEntityByUniqueId(String uniqueId, int requestingEntity) {
        ParentNodeComponent pnc = parentNodeMapper.get(requestingEntity);
        if (pnc == null) return -1;
        int parent = pnc.parentEntity;
        if (parent == -1) return -1;
        NodeComponent nc = nodeMapper.get(parent);
        if (nc == null) return -1;

        for (int child : nc.children) {
            MainItemComponent mic = mainItemMapper.get(child);
            if (mic != null && uniqueId.equals(mic.uniqueId)) {
                return child;
            }
        }
        return -1;
    }

    // ----------------------------------------------------------------
    // Checksum
    // ----------------------------------------------------------------

    private int calcChecksum(TalosAnchorConstraintComponent anchorComp, int entity) {
        int cs = 0;
        ParentNodeComponent parentNode = parentNodeMapper.get(entity);
        if (parentNode != null && parentNode.parentEntity != -1) {
            DimensionsComponent pd = dimensionsMapper.get(parentNode.parentEntity);
            if (pd != null) {
                cs += Float.floatToRawIntBits(pd.width) * 17;
                cs += Float.floatToRawIntBits(pd.height) * 19;
            }
        }

        TransformComponent tc = transformMapper.get(entity);
        if (tc != null) {
            cs += Float.floatToRawIntBits(tc.x) * 23;
            cs += Float.floatToRawIntBits(tc.y) * 29;
            cs += Float.floatToRawIntBits(tc.rotation) * 31;
            cs += Float.floatToRawIntBits(tc.scaleX) * 37;
            cs += Float.floatToRawIntBits(tc.scaleY) * 41;
            cs += Float.floatToRawIntBits(tc.originX) * 43;
            cs += Float.floatToRawIntBits(tc.originY) * 47;
        }

        Array<TalosAnchorConstraintComponent.AnchorBinding> bindings = anchorComp.bindings;
        for (int i = 0; i < bindings.size; i++) {
            TalosAnchorConstraintComponent.AnchorBinding b = bindings.get(i);
            cs += b.scopeKey * (53 + i * 7);
            cs += Float.floatToRawIntBits(b.horizontalBias) * (59 + i * 3);
            cs += Float.floatToRawIntBits(b.verticalBias) * (61 + i * 5);
            cs += constraintDataChecksum(b.left) * (67 + i * 11);
            cs += constraintDataChecksum(b.right) * (71 + i * 13);
            cs += constraintDataChecksum(b.top) * (73 + i * 17);
            cs += constraintDataChecksum(b.bottom) * (79 + i * 19);
        }

        return cs;
    }

    private int constraintDataChecksum(TalosAnchorConstraintComponent.ConstraintData data) {
        if (data == null) return 0;
        int cs = Float.floatToRawIntBits(data.margin) * 83 + data.targetEntity * 89;
        if (data.targetSide != null) cs += data.targetSide.ordinal() * 97;

        if (data.targetEntity != -1 && engine.getEntityManager().isActive(data.targetEntity)) {
            TransformComponent st = transformMapper.get(data.targetEntity);
            DimensionsComponent sd = dimensionsMapper.get(data.targetEntity);
            if (st != null && sd != null) {
                cs += Float.floatToRawIntBits(st.x) * 101;
                cs += Float.floatToRawIntBits(st.y) * 103;
                cs += Float.floatToRawIntBits(sd.width) * 107;
                cs += Float.floatToRawIntBits(sd.height) * 109;
            }
            BoundingBoxComponent bb = boundingBoxMapper.get(data.targetEntity);
            if (bb != null) cs += bb.checksum * 113;
        }

        return cs;
    }
}
