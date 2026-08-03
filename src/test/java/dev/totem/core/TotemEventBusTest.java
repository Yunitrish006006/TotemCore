package dev.totem.core;

import dev.totem.core.api.v1.event.DeathBackpackRecoveredEvent;
import dev.totem.core.api.v1.event.SpaceUnitPublicUpdateEvent;
import dev.totem.core.api.v1.event.TotemEventBus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TotemEventBusTest {
    @Test
    void dispatchesOnlyMatchingEventTypesAndSupportsUnsubscribe() {
        List<String> recoveredPlayers = new ArrayList<>();
        TotemEventBus.Subscription subscription = TotemEventBus.subscribe(
                DeathBackpackRecoveredEvent.class,
                event -> recoveredPlayers.add(event.playerName())
        );
        try {
            assertEquals(1, TotemEventBus.publish(new DeathBackpackRecoveredEvent("Alex")));
            assertEquals(0, TotemEventBus.publish(
                    new SpaceUnitPublicUpdateEvent("server", "updated")
            ));
        } finally {
            subscription.close();
        }

        assertEquals(0, TotemEventBus.publish(new DeathBackpackRecoveredEvent("Steve")));
        assertEquals(List.of("Alex"), recoveredPlayers);
    }

    @Test
    void isolatesSubscriberFailureFromCompletedGameplayAndOtherSubscribers() {
        List<String> delivered = new ArrayList<>();
        TotemEventBus.Subscription failing = TotemEventBus.subscribe(
                DeathBackpackRecoveredEvent.class,
                event -> {
                    throw new IllegalStateException("forced failure");
                }
        );
        TotemEventBus.Subscription healthy = TotemEventBus.subscribe(
                DeathBackpackRecoveredEvent.class,
                event -> delivered.add(event.playerName())
        );
        try {
            assertEquals(1, TotemEventBus.publish(new DeathBackpackRecoveredEvent("Alex")));
        } finally {
            failing.close();
            healthy.close();
        }

        assertEquals(List.of("Alex"), delivered);
    }
}
