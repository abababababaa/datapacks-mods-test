package dev.datapacksmods.cabletiers;

import appeng.api.networking.IGridNode;
import appeng.api.networking.pathing.ChannelMode;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.parts.networking.CablePart;
import appeng.me.pathfinding.IPathItem;

public final class CableCapacity {
    public static TieredCableItem variant(IGridNode node) {
        if (node.getOwner() instanceof CablePart part && part.getPartItem().asItem() instanceof TieredCableItem item) return item;
        return null;
    }
    public static int nodeCapacity(IGridNode node, int original) {
        var variant = variant(node);
        if (variant == null || original == 0 || original == Integer.MAX_VALUE) return original;
        ChannelMode mode = node.getGrid().getPathingService().getChannelMode();
        return CableConfig.baseCapacity(variant.kind, variant.tier) * mode.getCableCapacityFactor();
    }
    public static int connectionCapacity(IGridNode a, IGridNode b, int original) {
        if (original == Integer.MAX_VALUE || (variant(a) == null && variant(b) == null)) return original;
        // A controller face follows its attached cable. Other machines/P2P endpoints keep their own limits.
        int aMax = a.getOwner() instanceof ControllerBlockEntity ? Integer.MAX_VALUE : ((IPathItem) a).getMaxChannels();
        int bMax = b.getOwner() instanceof ControllerBlockEntity ? Integer.MAX_VALUE : ((IPathItem) b).getMaxChannels();
        return Math.min(aMax, bMax);
    }
    private CableCapacity() {}
}
