package dev.totem.core.api.v1.client.observer;

import java.util.Objects;
import java.util.UUID;

/** Display-only target identity and observer lifecycle callbacks. */
public record ObserverScreenContext(
        UUID targetId,
        String targetName,
        Runnable stopObserving
) {
    public ObserverScreenContext {
        Objects.requireNonNull(targetId, "targetId");
        targetName = bounded(targetName, 64, "targetName");
        Objects.requireNonNull(stopObserving, "stopObserving");
    }

    private static String bounded(String value, int max, String field) {
        if (value == null || value.length() > max) {
            throw new IllegalArgumentException("Invalid " + field);
        }
        return value;
    }
}
