package dev.totem.core.api.v1.client.world;

import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.SimpleGizmoCollector;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotemWorldOutlinesTest {
    @Test
    void submitsBothOcclusionModesWithoutRetainingState() {
        SimpleGizmoCollector collector = new SimpleGizmoCollector();

        try (var ignored = Gizmos.withCollector(collector)) {
            TotemWorldOutlines.cuboid(
                    new AABB(0, 0, 0, 1, 1, 1),
                    new WorldOutlineStyle(0xFFFFFFFF, 1.0F, WorldOutlineOcclusion.DEPTH_TESTED)
            );
            TotemWorldOutlines.block(
                    new BlockPos(2, 3, 4),
                    new WorldOutlineStyle(0xFF00FFFF, 2.0F, WorldOutlineOcclusion.THROUGH_WALLS)
            );
        }

        assertEquals(2, collector.getGizmos().size());
        assertFalse(collector.getGizmos().get(0).isAlwaysOnTop());
        assertTrue(collector.getGizmos().get(1).isAlwaysOnTop());
    }

    @Test
    void rejectsInvalidLineWidthsAndMissingOcclusion() {
        assertThrows(IllegalArgumentException.class, () ->
                new WorldOutlineStyle(0xFFFFFFFF, 0.0F, WorldOutlineOcclusion.DEPTH_TESTED));
        assertThrows(IllegalArgumentException.class, () ->
                new WorldOutlineStyle(0xFFFFFFFF, Float.NaN, WorldOutlineOcclusion.DEPTH_TESTED));
        assertThrows(NullPointerException.class, () ->
                new WorldOutlineStyle(0xFFFFFFFF, 1.0F, null));
    }
}
