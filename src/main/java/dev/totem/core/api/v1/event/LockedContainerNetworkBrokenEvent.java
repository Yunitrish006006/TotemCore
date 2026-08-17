package dev.totem.core.api.v1.event;

import java.util.Objects;
import java.util.UUID;

/**
 * Published after a player successfully removes one member of a locked fixed-container network.
 * The event deliberately contains no inventory, ACL, key, or complete topology data.
 */
public record LockedContainerNetworkBrokenEvent(
        UUID eventId,
        UUID lockId,
        UUID actorUuid,
        String actorName,
        UUID ownerUuid,
        String ownerName,
        String brokenMemberKind,
        String dimension,
        int x,
        int y,
        int z,
        int remainingLockedContainers,
        int detachedUnlockedContainers,
        boolean rootMoved,
        boolean lockRemoved,
        long occurredAtEpochMillis
) implements TotemEvent {
    private static final int MAX_NAME_CODE_POINTS = 64;
    private static final int MAX_KIND_CODE_POINTS = 64;
    private static final int MAX_DIMENSION_CODE_POINTS = 128;

    public LockedContainerNetworkBrokenEvent {
        eventId = Objects.requireNonNull(eventId, "eventId");
        lockId = Objects.requireNonNull(lockId, "lockId");
        actorUuid = Objects.requireNonNull(actorUuid, "actorUuid");
        ownerUuid = Objects.requireNonNull(ownerUuid, "ownerUuid");
        actorName = sanitize(actorName, MAX_NAME_CODE_POINTS, "actorName");
        ownerName = sanitize(ownerName, MAX_NAME_CODE_POINTS, "ownerName");
        brokenMemberKind = sanitize(brokenMemberKind, MAX_KIND_CODE_POINTS, "brokenMemberKind");
        dimension = sanitize(dimension, MAX_DIMENSION_CODE_POINTS, "dimension");
        if (remainingLockedContainers < 0 || detachedUnlockedContainers < 0) {
            throw new IllegalArgumentException("container counts must be non-negative");
        }
        if (lockRemoved != (remainingLockedContainers == 0)) {
            throw new IllegalArgumentException(
                    "lockRemoved must equal remainingLockedContainers == 0");
        }
        if (occurredAtEpochMillis < 0L) {
            throw new IllegalArgumentException("occurredAtEpochMillis must be non-negative");
        }
    }

    @Override
    public int contractVersion() {
        return 1;
    }

    private static String sanitize(String value, int maximumCodePoints, String field) {
        Objects.requireNonNull(value, field);
        StringBuilder cleaned = new StringBuilder();
        value.codePoints()
                .filter(codePoint -> !Character.isISOControl(codePoint))
                .limit(maximumCodePoints)
                .forEach(cleaned::appendCodePoint);
        String result = cleaned.toString().trim();
        if (result.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return result;
    }
}
