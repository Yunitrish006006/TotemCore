package dev.totem.core.api.v1.client.world;

import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.LineGizmo;
import net.minecraft.gizmos.SimpleGizmoCollector;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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

    @Test
    void submitsExactLineGeometryAndBothOcclusionModes() {
        Vec3 from = new Vec3(1.25D, 2.5D, 3.75D);
        Vec3 to = new Vec3(8.0D, 9.5D, 10.0D);
        WorldOutlineStyle depthTested = new WorldOutlineStyle(
                0xFF66BB6A,
                1.75F,
                WorldOutlineOcclusion.DEPTH_TESTED
        );
        WorldOutlineStyle throughWalls = new WorldOutlineStyle(
                0xFFEF5350,
                2.25F,
                WorldOutlineOcclusion.THROUGH_WALLS
        );
        SimpleGizmoCollector collector = new SimpleGizmoCollector();

        try (var ignored = Gizmos.withCollector(collector)) {
            TotemWorldOutlines.line(from, to, depthTested);
            TotemWorldOutlines.line(to, from, throughWalls);
        }

        assertEquals(2, collector.getGizmos().size());
        LineGizmo first = assertInstanceOf(LineGizmo.class,
                collector.getGizmos().get(0).gizmo());
        assertEquals(from, first.start());
        assertEquals(to, first.end());
        assertEquals(depthTested.argb(), first.color());
        assertEquals(depthTested.lineWidth(), first.width());
        assertFalse(collector.getGizmos().get(0).isAlwaysOnTop());

        LineGizmo second = assertInstanceOf(LineGizmo.class,
                collector.getGizmos().get(1).gizmo());
        assertEquals(to, second.start());
        assertEquals(from, second.end());
        assertEquals(throughWalls.argb(), second.color());
        assertEquals(throughWalls.lineWidth(), second.width());
        assertTrue(collector.getGizmos().get(1).isAlwaysOnTop());
    }

    @Test
    void rejectsMissingLineArguments() {
        WorldOutlineStyle style = new WorldOutlineStyle(
                0xFFFFFFFF,
                1.0F,
                WorldOutlineOcclusion.DEPTH_TESTED
        );

        assertThrows(NullPointerException.class, () ->
                TotemWorldOutlines.line(null, Vec3.ZERO, style));
        assertThrows(NullPointerException.class, () ->
                TotemWorldOutlines.line(Vec3.ZERO, null, style));
        assertThrows(NullPointerException.class, () ->
                TotemWorldOutlines.line(Vec3.ZERO, Vec3.ZERO, null));
    }
}
