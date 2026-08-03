package dev.totem.core.api.v1.event;

import java.util.Objects;

/** Published after Remnant commits a death backpack successfully. */
public record DeathBackpackCreatedEvent(
        String playerName,
        int stackCount,
        String dimensionId,
        int x,
        int y,
        int z
) implements TotemEvent {
    public DeathBackpackCreatedEvent {
        playerName = Objects.requireNonNull(playerName, "playerName");
        dimensionId = Objects.requireNonNull(dimensionId, "dimensionId");
        if (stackCount < 0) {
            throw new IllegalArgumentException("stackCount must not be negative");
        }
    }

    @Override
    public int contractVersion() {
        return 1;
    }
}
