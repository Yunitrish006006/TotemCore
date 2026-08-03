package dev.totem.core.api.v1.event;

import java.util.Objects;

/** Published when the public Space Unit projection changes. */
public record SpaceUnitPublicUpdateEvent(String actor, String message) implements TotemEvent {
    public SpaceUnitPublicUpdateEvent {
        actor = Objects.requireNonNull(actor, "actor");
        message = Objects.requireNonNull(message, "message");
    }

    @Override
    public int contractVersion() {
        return 1;
    }
}
