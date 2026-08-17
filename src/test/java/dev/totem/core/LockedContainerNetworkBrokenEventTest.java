package dev.totem.core;

import dev.totem.core.api.v1.event.LockedContainerNetworkBrokenEvent;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LockedContainerNetworkBrokenEventTest {
    private static final UUID EVENT_ID = UUID.fromString("64c459dd-48d0-471e-9d70-f21bbb20d7cb");
    private static final UUID LOCK_ID = UUID.fromString("029dc2a8-1fc3-4c8c-954e-7f070ad2023e");
    private static final UUID ACTOR_ID = UUID.fromString("9f3ab0bb-026d-4777-a578-dd6f4e50571c");
    private static final UUID OWNER_ID = UUID.fromString("1b0bf873-878c-4a60-8e75-376157249774");

    @Test
    void validatesInvariantAndSanitizesDisplayFields() {
        LockedContainerNetworkBrokenEvent event = event(2, 3, false, "A\nctor", " hopper ");
        assertEquals("Actor", event.actorName());
        assertEquals("hopper", event.brokenMemberKind());
        assertEquals(1, event.contractVersion());
    }

    @Test
    void lockRemovedMustMatchRemainingCount() {
        assertThrows(IllegalArgumentException.class,
                () -> event(1, 0, true, "Actor", "chest"));
        assertThrows(IllegalArgumentException.class,
                () -> event(0, 0, false, "Actor", "chest"));
    }

    @Test
    void countsCannotBeNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> event(-1, 0, false, "Actor", "chest"));
        assertThrows(IllegalArgumentException.class,
                () -> event(1, -1, false, "Actor", "chest"));
    }

    private static LockedContainerNetworkBrokenEvent event(
            int remaining,
            int detached,
            boolean removed,
            String actor,
            String kind
    ) {
        return new LockedContainerNetworkBrokenEvent(
                EVENT_ID,
                LOCK_ID,
                ACTOR_ID,
                actor,
                OWNER_ID,
                "Owner",
                kind,
                "minecraft:overworld",
                1,
                64,
                2,
                remaining,
                detached,
                false,
                removed,
                1L
        );
    }
}
