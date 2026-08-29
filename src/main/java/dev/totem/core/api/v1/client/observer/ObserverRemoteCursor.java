package dev.totem.core.api.v1.client.observer;

import net.minecraft.world.item.ItemStack;

/** Bounded logical cursor state, independent of either client's GUI scale. */
public record ObserverRemoteCursor(
        long sequence,
        float logicalX,
        float logicalY,
        int contentWidth,
        int contentHeight,
        ItemStack carriedStack
) {
    public static final int MAX_CONTENT_SIZE = 4096;

    public ObserverRemoteCursor {
        if (sequence < 0 || !Float.isFinite(logicalX) || !Float.isFinite(logicalY)) {
            throw new IllegalArgumentException("Invalid observer cursor sequence or coordinate");
        }
        if (contentWidth < 1 || contentWidth > MAX_CONTENT_SIZE
                || contentHeight < 1 || contentHeight > MAX_CONTENT_SIZE) {
            throw new IllegalArgumentException("Invalid observer cursor content geometry");
        }
        logicalX = Math.clamp(logicalX, 0.0F, contentWidth - 1.0F);
        logicalY = Math.clamp(logicalY, 0.0F, contentHeight - 1.0F);
        carriedStack = carriedStack == null ? ItemStack.EMPTY : carriedStack.copy();
        // The wire codec performs the item-specific component validation after
        // registry bootstrap. This API-level cap is deliberately independent
        // of registries so pure contract tests remain deterministic.
        if (carriedStack.getCount() > 99) {
            throw new IllegalArgumentException("Observer cursor stack exceeds the transport cap");
        }
    }

    public double screenX(int contentLeft, int renderedWidth) {
        return contentLeft + logicalX * renderedWidth / contentWidth;
    }

    public double screenY(int contentTop, int renderedHeight) {
        return contentTop + logicalY * renderedHeight / contentHeight;
    }

    @Override public ItemStack carriedStack() { return carriedStack.copy(); }
}
