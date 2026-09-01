package dev.totem.core.api.v1.client.world;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Immutable exterior wireframe plan for a union of unit block voxels.
 *
 * <p>Face-shared edges, coplanar face seams and fully enclosed cavity surfaces
 * are omitted. Remaining collinear unit edges are merged into deterministic,
 * maximal line segments. Face-connected components are processed independently
 * so distant disconnected inputs do not create one enormous flood-fill volume.
 * A single component whose one-block-padded envelope exceeds the bounded safety
 * limit is rejected instead of allocating work proportional to a pathological
 * sparse bounding box.</p>
 */
@Environment(EnvType.CLIENT)
public final class VoxelUnionOutline {
    static final long MAX_COMPONENT_ENVELOPE_CELLS = 1_000_000L;
    private static final VoxelUnionOutline EMPTY = new VoxelUnionOutline(List.of());
    private static final Comparator<Voxel> VOXEL_ORDER = Comparator
            .comparingLong(Voxel::x)
            .thenComparingLong(Voxel::y)
            .thenComparingLong(Voxel::z);
    private static final Comparator<LineKey> LINE_KEY_ORDER = Comparator
            .<LineKey>comparingInt(key -> key.axis().ordinal())
            .thenComparingLong(LineKey::fixedFirst)
            .thenComparingLong(LineKey::fixedSecond);

    private final List<Segment> segments;

    private VoxelUnionOutline(List<Segment> segments) {
        this.segments = List.copyOf(segments);
    }

    /**
     * Derives an immutable exterior outline from the supplied block positions.
     * Duplicate positions are ignored and caller-owned mutable positions are
     * copied by value.
     *
     * @throws NullPointerException if the collection or any position is null
     * @throws IllegalArgumentException if one face-connected component has a
     *         padded envelope larger than the documented safety limit
     */
    public static VoxelUnionOutline of(Collection<? extends BlockPos> positions) {
        Objects.requireNonNull(positions, "positions");
        Set<Voxel> occupied = new HashSet<>();
        for (BlockPos position : positions) {
            Objects.requireNonNull(position, "positions contains null");
            occupied.add(new Voxel(position.getX(), position.getY(), position.getZ()));
        }
        if (occupied.isEmpty()) {
            return EMPTY;
        }

        Map<UnitEdge, EnumSet<Face>> edgeFaces = new HashMap<>();
        Set<Voxel> remaining = new HashSet<>(occupied);
        List<Voxel> deterministicSeeds = occupied.stream().sorted(VOXEL_ORDER).toList();
        for (Voxel seed : deterministicSeeds) {
            if (!remaining.remove(seed)) {
                continue;
            }
            Set<Voxel> component = faceConnectedComponent(seed, occupied, remaining);
            collectExteriorFaceEdges(component, edgeFaces);
        }

        Set<UnitEdge> exteriorEdges = new HashSet<>();
        edgeFaces.forEach((edge, faces) -> {
            if (faces.size() > 1) {
                exteriorEdges.add(edge);
            }
        });
        return new VoxelUnionOutline(mergeCollinearEdges(exteriorEdges));
    }

    /** Returns the deterministic maximal segments in positive-axis order. */
    public List<Segment> segments() {
        return segments;
    }

    public int segmentCount() {
        return segments.size();
    }

    private static Set<Voxel> faceConnectedComponent(
            Voxel seed,
            Set<Voxel> occupied,
            Set<Voxel> remaining) {
        Set<Voxel> component = new HashSet<>();
        ArrayDeque<Voxel> queue = new ArrayDeque<>();
        component.add(seed);
        queue.add(seed);
        while (!queue.isEmpty()) {
            Voxel current = queue.removeFirst();
            for (Face face : Face.values()) {
                Voxel neighbor = current.move(face.dx(), face.dy(), face.dz());
                if (occupied.contains(neighbor) && component.add(neighbor)) {
                    remaining.remove(neighbor);
                    queue.addLast(neighbor);
                }
            }
        }
        return component;
    }

    private static void collectExteriorFaceEdges(
            Set<Voxel> component,
            Map<UnitEdge, EnumSet<Face>> edgeFaces) {
        Bounds bounds = Bounds.around(component);
        long envelopeVolume = bounds.volume();
        if (envelopeVolume > MAX_COMPONENT_ENVELOPE_CELLS) {
            throw new IllegalArgumentException(
                    "Voxel component exterior envelope is too large: " + envelopeVolume
                            + " cells (maximum " + MAX_COMPONENT_ENVELOPE_CELLS + ")");
        }

        Set<Voxel> exteriorAir = exteriorAir(component, bounds, Math.toIntExact(envelopeVolume));
        for (Voxel voxel : component) {
            for (Face face : Face.values()) {
                if (!exteriorAir.contains(voxel.move(face.dx(), face.dy(), face.dz()))) {
                    continue;
                }
                for (UnitEdge edge : face.edges(voxel)) {
                    edgeFaces.computeIfAbsent(edge, ignored -> EnumSet.noneOf(Face.class)).add(face);
                }
            }
        }
    }

    private static Set<Voxel> exteriorAir(Set<Voxel> component, Bounds bounds, int envelopeVolume) {
        Set<Voxel> exterior = new HashSet<>(Math.min(envelopeVolume, 1 << 16));
        ArrayDeque<Voxel> queue = new ArrayDeque<>();
        Voxel start = new Voxel(bounds.minX(), bounds.minY(), bounds.minZ());
        exterior.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            Voxel current = queue.removeFirst();
            for (Face face : Face.values()) {
                Voxel neighbor = current.move(face.dx(), face.dy(), face.dz());
                if (bounds.contains(neighbor)
                        && !component.contains(neighbor)
                        && exterior.add(neighbor)) {
                    queue.addLast(neighbor);
                }
            }
        }
        return exterior;
    }

    private static List<Segment> mergeCollinearEdges(Set<UnitEdge> edges) {
        NavigableMap<LineKey, NavigableSet<Long>> startsByLine = new TreeMap<>(LINE_KEY_ORDER);
        for (UnitEdge edge : edges) {
            LineKey key = edge.lineKey();
            startsByLine.computeIfAbsent(key, ignored -> new TreeSet<>()).add(edge.varyingStart());
        }

        List<Segment> segments = new ArrayList<>();
        startsByLine.forEach((key, starts) -> {
            Long runStart = null;
            long runEnd = 0L;
            for (long start : starts) {
                if (runStart == null) {
                    runStart = start;
                    runEnd = start + 1L;
                } else if (start == runEnd) {
                    runEnd++;
                } else {
                    segments.add(key.segment(runStart, runEnd));
                    runStart = start;
                    runEnd = start + 1L;
                }
            }
            if (runStart != null) {
                segments.add(key.segment(runStart, runEnd));
            }
        });
        return segments;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof VoxelUnionOutline outline && segments.equals(outline.segments);
    }

    @Override
    public int hashCode() {
        return segments.hashCode();
    }

    @Override
    public String toString() {
        return "VoxelUnionOutline[segments=" + segments + ']';
    }

    /** One immutable, non-zero, axis-aligned outline segment. */
    public record Segment(Vec3 from, Vec3 to) {
        public Segment {
            Objects.requireNonNull(from, "from");
            Objects.requireNonNull(to, "to");
            if (!finite(from) || !finite(to)) {
                throw new IllegalArgumentException("Voxel outline segment coordinates must be finite");
            }
            int changedAxes = (Double.compare(from.x, to.x) == 0 ? 0 : 1)
                    + (Double.compare(from.y, to.y) == 0 ? 0 : 1)
                    + (Double.compare(from.z, to.z) == 0 ? 0 : 1);
            if (changedAxes != 1) {
                throw new IllegalArgumentException("Voxel outline segment must be non-zero and axis-aligned");
            }
        }

        private static boolean finite(Vec3 point) {
            return Double.isFinite(point.x) && Double.isFinite(point.y) && Double.isFinite(point.z);
        }
    }

    private enum Axis {
        X,
        Y,
        Z
    }

    private enum Face {
        NEGATIVE_X(-1, 0, 0),
        POSITIVE_X(1, 0, 0),
        NEGATIVE_Y(0, -1, 0),
        POSITIVE_Y(0, 1, 0),
        NEGATIVE_Z(0, 0, -1),
        POSITIVE_Z(0, 0, 1);

        private final int dx;
        private final int dy;
        private final int dz;

        Face(int dx, int dy, int dz) {
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
        }

        private int dx() {
            return dx;
        }

        private int dy() {
            return dy;
        }

        private int dz() {
            return dz;
        }

        private List<UnitEdge> edges(Voxel voxel) {
            long x = voxel.x();
            long y = voxel.y();
            long z = voxel.z();
            return switch (this) {
                case NEGATIVE_X -> List.of(
                        new UnitEdge(Axis.Y, x, y, z),
                        new UnitEdge(Axis.Y, x, y, z + 1L),
                        new UnitEdge(Axis.Z, x, y, z),
                        new UnitEdge(Axis.Z, x, y + 1L, z));
                case POSITIVE_X -> List.of(
                        new UnitEdge(Axis.Y, x + 1L, y, z),
                        new UnitEdge(Axis.Y, x + 1L, y, z + 1L),
                        new UnitEdge(Axis.Z, x + 1L, y, z),
                        new UnitEdge(Axis.Z, x + 1L, y + 1L, z));
                case NEGATIVE_Y -> List.of(
                        new UnitEdge(Axis.X, x, y, z),
                        new UnitEdge(Axis.X, x, y, z + 1L),
                        new UnitEdge(Axis.Z, x, y, z),
                        new UnitEdge(Axis.Z, x + 1L, y, z));
                case POSITIVE_Y -> List.of(
                        new UnitEdge(Axis.X, x, y + 1L, z),
                        new UnitEdge(Axis.X, x, y + 1L, z + 1L),
                        new UnitEdge(Axis.Z, x, y + 1L, z),
                        new UnitEdge(Axis.Z, x + 1L, y + 1L, z));
                case NEGATIVE_Z -> List.of(
                        new UnitEdge(Axis.X, x, y, z),
                        new UnitEdge(Axis.X, x, y + 1L, z),
                        new UnitEdge(Axis.Y, x, y, z),
                        new UnitEdge(Axis.Y, x + 1L, y, z));
                case POSITIVE_Z -> List.of(
                        new UnitEdge(Axis.X, x, y, z + 1L),
                        new UnitEdge(Axis.X, x, y + 1L, z + 1L),
                        new UnitEdge(Axis.Y, x, y, z + 1L),
                        new UnitEdge(Axis.Y, x + 1L, y, z + 1L));
            };
        }
    }

    private record Voxel(long x, long y, long z) {
        private Voxel move(long dx, long dy, long dz) {
            return new Voxel(x + dx, y + dy, z + dz);
        }
    }

    private record UnitEdge(Axis axis, long x, long y, long z) {
        private LineKey lineKey() {
            return switch (axis) {
                case X -> new LineKey(axis, y, z);
                case Y -> new LineKey(axis, x, z);
                case Z -> new LineKey(axis, x, y);
            };
        }

        private long varyingStart() {
            return switch (axis) {
                case X -> x;
                case Y -> y;
                case Z -> z;
            };
        }
    }

    private record LineKey(Axis axis, long fixedFirst, long fixedSecond) {
        private Segment segment(long start, long end) {
            return switch (axis) {
                case X -> new Segment(
                        new Vec3(start, fixedFirst, fixedSecond),
                        new Vec3(end, fixedFirst, fixedSecond));
                case Y -> new Segment(
                        new Vec3(fixedFirst, start, fixedSecond),
                        new Vec3(fixedFirst, end, fixedSecond));
                case Z -> new Segment(
                        new Vec3(fixedFirst, fixedSecond, start),
                        new Vec3(fixedFirst, fixedSecond, end));
            };
        }
    }

    private record Bounds(long minX, long minY, long minZ, long maxX, long maxY, long maxZ) {
        private static Bounds around(Set<Voxel> component) {
            long minX = Long.MAX_VALUE;
            long minY = Long.MAX_VALUE;
            long minZ = Long.MAX_VALUE;
            long maxX = Long.MIN_VALUE;
            long maxY = Long.MIN_VALUE;
            long maxZ = Long.MIN_VALUE;
            for (Voxel voxel : component) {
                minX = Math.min(minX, voxel.x());
                minY = Math.min(minY, voxel.y());
                minZ = Math.min(minZ, voxel.z());
                maxX = Math.max(maxX, voxel.x());
                maxY = Math.max(maxY, voxel.y());
                maxZ = Math.max(maxZ, voxel.z());
            }
            return new Bounds(minX - 1L, minY - 1L, minZ - 1L,
                    maxX + 1L, maxY + 1L, maxZ + 1L);
        }

        private long volume() {
            try {
                long width = Math.addExact(Math.subtractExact(maxX, minX), 1L);
                long height = Math.addExact(Math.subtractExact(maxY, minY), 1L);
                long depth = Math.addExact(Math.subtractExact(maxZ, minZ), 1L);
                return Math.multiplyExact(Math.multiplyExact(width, height), depth);
            } catch (ArithmeticException exception) {
                throw new IllegalArgumentException("Voxel component exterior envelope overflows", exception);
            }
        }

        private boolean contains(Voxel voxel) {
            return voxel.x() >= minX && voxel.x() <= maxX
                    && voxel.y() >= minY && voxel.y() <= maxY
                    && voxel.z() >= minZ && voxel.z() <= maxZ;
        }
    }
}
