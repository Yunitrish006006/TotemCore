package dev.totem.core.api.v1.client.world;

import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.LineGizmo;
import net.minecraft.gizmos.SimpleGizmoCollector;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VoxelUnionOutlineTest {
    @Test
    void emptyInputProducesTheImmutableEmptyPlan() {
        VoxelUnionOutline outline = VoxelUnionOutline.of(List.of());

        assertEquals(0, outline.segmentCount());
        assertTrue(outline.segments().isEmpty());
        assertThrows(UnsupportedOperationException.class, () ->
                outline.segments().add(segment(0, 0, 0, 1, 0, 0)));
    }

    @Test
    void singleCubeProducesTwelveUnitSegments() {
        VoxelUnionOutline outline = VoxelUnionOutline.of(List.of(new BlockPos(2, 3, 4)));

        assertEquals(12, outline.segmentCount());
        assertTrue(outline.segments().stream().allMatch(segment -> length(segment) == 1.0D));
    }

    @Test
    void adjacentRowCollapsesToTwelveMaximalCuboidSegments() {
        VoxelUnionOutline outline = VoxelUnionOutline.of(List.of(
                new BlockPos(0, 0, 0),
                new BlockPos(1, 0, 0),
                new BlockPos(2, 0, 0)
        ));

        assertEquals(12, outline.segmentCount());
        assertEquals(4, outline.segments().stream().filter(segment -> length(segment) == 3.0D).count());
        assertEquals(8, outline.segments().stream().filter(segment -> length(segment) == 1.0D).count());
    }

    @Test
    void coplanarPlateHasNoSharedFaceOrSurfaceGridSeams() {
        VoxelUnionOutline outline = VoxelUnionOutline.of(List.of(
                new BlockPos(0, 0, 0),
                new BlockPos(1, 0, 0),
                new BlockPos(0, 1, 0),
                new BlockPos(1, 1, 0)
        ));

        assertEquals(12, outline.segmentCount());
        assertEquals(8, outline.segments().stream().filter(segment -> length(segment) == 2.0D).count());
        assertEquals(4, outline.segments().stream().filter(segment -> length(segment) == 1.0D).count());
        assertFalse(outline.segments().contains(segment(1, 0, 0, 1, 2, 0)));
        assertFalse(outline.segments().contains(segment(0, 1, 1, 2, 1, 1)));
    }

    @Test
    void irregularUnionPreservesItsConcaveExteriorInsteadOfUsingAGlobalBox() {
        VoxelUnionOutline outline = VoxelUnionOutline.of(List.of(
                new BlockPos(0, 0, 0),
                new BlockPos(1, 0, 0),
                new BlockPos(0, 1, 0)
        ));

        assertEquals(18, outline.segmentCount());
        assertTrue(outline.segments().contains(segment(1, 1, 0, 1, 1, 1)));
        assertTrue(outline.segments().contains(segment(1, 1, 0, 2, 1, 0)));
        assertFalse(outline.segments().contains(segment(0, 2, 0, 2, 2, 0)));
    }

    @Test
    void distantDisconnectedBlocksRemainSeparateWithoutOneGlobalFloodVolume() {
        VoxelUnionOutline outline = VoxelUnionOutline.of(List.of(
                new BlockPos(-30_000_000, 0, -30_000_000),
                new BlockPos(30_000_000, 0, 30_000_000)
        ));

        assertEquals(24, outline.segmentCount());
        assertTrue(outline.segments().contains(
                segment(-30_000_000, 0, -30_000_000, -29_999_999, 0, -30_000_000)));
        assertTrue(outline.segments().contains(
                segment(30_000_000, 0, 30_000_000, 30_000_001, 0, 30_000_000)));
    }

    @Test
    void edgeAndCornerTouchingFaceDisconnectedComponentsRetainTheirGeometry() {
        VoxelUnionOutline edgeTouching = VoxelUnionOutline.of(List.of(
                new BlockPos(0, 0, 0),
                new BlockPos(1, 1, 0)
        ));
        VoxelUnionOutline cornerTouching = VoxelUnionOutline.of(List.of(
                new BlockPos(0, 0, 0),
                new BlockPos(1, 1, 1)
        ));

        // The exact union keeps the shared vertical edge once and merges four
        // collinear pairs across the contact point: 24 - 1 - 4 = 19.
        assertEquals(19, edgeTouching.segmentCount());
        assertEquals(1, edgeTouching.segments().stream()
                .filter(segment -> segment.equals(segment(1, 1, 0, 1, 1, 1)))
                .count());
        // Three positive-axis pairs meet at the shared corner and become three
        // maximal lines without bridging any air gap: 24 - 3 = 21.
        assertEquals(21, cornerTouching.segmentCount());
    }

    @Test
    void integerCoordinateLimitsDoNotOverflowNeighborsOrSegmentEndpoints() {
        VoxelUnionOutline outline = VoxelUnionOutline.of(List.of(
                new BlockPos(Integer.MIN_VALUE, 0, 0),
                new BlockPos(Integer.MAX_VALUE, 0, 0)
        ));

        assertEquals(24, outline.segmentCount());
        assertTrue(outline.segments().contains(segment(
                Integer.MIN_VALUE, 0, 0, (double) Integer.MIN_VALUE + 1.0D, 0, 0)));
        assertTrue(outline.segments().contains(segment(
                Integer.MAX_VALUE, 0, 0, (double) Integer.MAX_VALUE + 1.0D, 0, 0)));
    }

    @Test
    void fullyEnclosedCavityDoesNotCreateAnInnerWireframe() {
        List<BlockPos> shell = new ArrayList<>();
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    if (x != 1 || y != 1 || z != 1) {
                        shell.add(new BlockPos(x, y, z));
                    }
                }
            }
        }

        VoxelUnionOutline outline = VoxelUnionOutline.of(shell);

        assertEquals(12, outline.segmentCount());
        assertTrue(outline.segments().stream().allMatch(segment -> length(segment) == 3.0D));
        assertFalse(outline.segments().stream().anyMatch(segment ->
                touchesCoordinate(segment, 1.0D) || touchesCoordinate(segment, 2.0D)));
    }

    @Test
    void resultIsDeterministicImmutableAndDeduplicatesCopiedInputs() {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(0, 0, 0);
        List<BlockPos> positions = new ArrayList<>(List.of(
                mutable,
                new BlockPos(0, 0, 0),
                new BlockPos(1, 0, 0)
        ));
        VoxelUnionOutline first = VoxelUnionOutline.of(positions);

        mutable.set(20, 20, 20);
        positions.clear();
        List<BlockPos> reversed = new ArrayList<>(List.of(
                new BlockPos(1, 0, 0),
                new BlockPos(0, 0, 0)
        ));
        Collections.reverse(reversed);
        VoxelUnionOutline second = VoxelUnionOutline.of(reversed);

        assertEquals(12, first.segmentCount());
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertThrows(UnsupportedOperationException.class, () -> first.segments().clear());
        assertThrows(NullPointerException.class, () -> VoxelUnionOutline.of(null));
        assertThrows(NullPointerException.class, () ->
                VoxelUnionOutline.of(java.util.Arrays.asList(new BlockPos(0, 0, 0), null)));
        assertThrows(NullPointerException.class, () -> new VoxelUnionOutline.Segment(null, Vec3.ZERO));
        assertThrows(IllegalArgumentException.class, () ->
                new VoxelUnionOutline.Segment(Vec3.ZERO, new Vec3(1, 1, 0)));
        assertThrows(IllegalArgumentException.class, () ->
                new VoxelUnionOutline.Segment(Vec3.ZERO, Vec3.ZERO));
    }

    @Test
    void rejectsPathologicalSparseConnectedEnvelopeBeforeFloodingIt() {
        List<BlockPos> staircase = new ArrayList<>();
        staircase.add(BlockPos.ZERO);
        for (int step = 1; step <= 101; step++) {
            staircase.add(new BlockPos(step, step - 1, step - 1));
            staircase.add(new BlockPos(step, step, step - 1));
            staircase.add(new BlockPos(step, step, step));
        }

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> VoxelUnionOutline.of(staircase));
        assertTrue(exception.getMessage().contains("exterior envelope is too large"));
    }

    @Test
    void cachedPlanSubmitsEverySegmentWithTheRequestedStyleAndOcclusion() {
        VoxelUnionOutline outline = VoxelUnionOutline.of(List.of(BlockPos.ZERO));
        WorldOutlineStyle depthTested = new WorldOutlineStyle(
                0xFF66BB6A, 1.5F, WorldOutlineOcclusion.DEPTH_TESTED);
        WorldOutlineStyle throughWalls = new WorldOutlineStyle(
                0xFF4FC3F7, 3.0F, WorldOutlineOcclusion.THROUGH_WALLS);
        SimpleGizmoCollector collector = new SimpleGizmoCollector();

        try (var ignored = Gizmos.withCollector(collector)) {
            TotemWorldOutlines.submit(outline, depthTested);
            TotemWorldOutlines.submit(outline, throughWalls);
        }

        assertEquals(24, collector.getGizmos().size());
        for (int index = 0; index < collector.getGizmos().size(); index++) {
            var entry = collector.getGizmos().get(index);
            LineGizmo line = assertInstanceOf(LineGizmo.class, entry.gizmo());
            WorldOutlineStyle expected = index < 12 ? depthTested : throughWalls;
            assertEquals(expected.argb(), line.color());
            assertEquals(expected.lineWidth(), line.width());
            assertEquals(index >= 12, entry.isAlwaysOnTop());
        }
        assertThrows(NullPointerException.class, () -> TotemWorldOutlines.submit(null, depthTested));
        assertThrows(NullPointerException.class, () -> TotemWorldOutlines.submit(outline, null));
    }

    private static VoxelUnionOutline.Segment segment(
            double fromX,
            double fromY,
            double fromZ,
            double toX,
            double toY,
            double toZ) {
        return new VoxelUnionOutline.Segment(
                new Vec3(fromX, fromY, fromZ),
                new Vec3(toX, toY, toZ));
    }

    private static double length(VoxelUnionOutline.Segment segment) {
        return segment.from().distanceTo(segment.to());
    }

    private static boolean touchesCoordinate(VoxelUnionOutline.Segment segment, double coordinate) {
        return segment.from().x == coordinate || segment.from().y == coordinate || segment.from().z == coordinate
                || segment.to().x == coordinate || segment.to().y == coordinate || segment.to().z == coordinate;
    }
}
