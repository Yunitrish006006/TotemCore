package dev.totem.core.api.v1.event;

import java.util.Objects;

/** Published after a module-owned administrative mutation completes. */
public record AdminAuditEvent(
        String actor,
        String action,
        String targetSummary
) implements TotemEvent {
    public AdminAuditEvent {
        actor = Objects.requireNonNull(actor, "actor");
        action = Objects.requireNonNull(action, "action");
        targetSummary = Objects.requireNonNull(targetSummary, "targetSummary");
    }

    @Override
    public int contractVersion() {
        return 1;
    }
}
