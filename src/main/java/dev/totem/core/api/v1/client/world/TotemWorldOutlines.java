package dev.totem.core.api.v1.client.world;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.AABB;

import java.util.Objects;

/** Stateless submission helpers for module-owned world-outline render callbacks. */
@Environment(EnvType.CLIENT)
public final class TotemWorldOutlines {
    private TotemWorldOutlines() {
    }

    public static void block(BlockPos position, WorldOutlineStyle style) {
        Objects.requireNonNull(position, "position");
        cuboid(new AABB(
                position.getX(),
                position.getY(),
                position.getZ(),
                position.getX() + 1.0D,
                position.getY() + 1.0D,
                position.getZ() + 1.0D
        ), style);
    }

    public static void cuboid(AABB bounds, WorldOutlineStyle style) {
        Objects.requireNonNull(bounds, "bounds");
        Objects.requireNonNull(style, "style");

        var properties = Gizmos.cuboid(
                bounds,
                GizmoStyle.stroke(style.argb(), style.lineWidth())
        );
        if (style.occlusion() == WorldOutlineOcclusion.THROUGH_WALLS) {
            properties.setAlwaysOnTop();
        }
    }
}
