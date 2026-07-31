package dev.totem.core.api.v1.event;

import java.util.Objects;

/** Published after a bound death backpack disables its Nexus death node. */
public record DeathBackpackRecoveredEvent(String playerName) implements TotemEvent {
    public DeathBackpackRecoveredEvent {
        playerName = Objects.requireNonNull(playerName, "playerName");
    }

    @Override
    public int contractVersion() {
        return 1;
    }
}
