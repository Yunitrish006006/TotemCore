package dev.totem.core.api.v1.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Process-local event bus for optional communication between Totem feature
 * modules. Publishers and subscribers only depend on Core contracts.
 */
public final class TotemEventBus {
    private static final Logger LOGGER = LoggerFactory.getLogger("TotemCoreEvents");
    private static final CopyOnWriteArrayList<ListenerRegistration<?>> LISTENERS =
            new CopyOnWriteArrayList<>();

    private TotemEventBus() {
    }

    public static <E extends TotemEvent> Subscription subscribe(
            Class<E> eventType,
            Consumer<? super E> listener
    ) {
        ListenerRegistration<E> registration = new ListenerRegistration<>(
                Objects.requireNonNull(eventType, "eventType"),
                Objects.requireNonNull(listener, "listener")
        );
        LISTENERS.add(registration);
        return () -> LISTENERS.remove(registration);
    }

    /**
     * Publishes one immutable event. A failed optional subscriber is logged and
     * isolated so it cannot roll back completed gameplay.
     *
     * @return number of subscribers that accepted the event successfully
     */
    public static int publish(TotemEvent event) {
        Objects.requireNonNull(event, "event");
        int delivered = 0;
        for (ListenerRegistration<?> registration : LISTENERS) {
            if (!registration.eventType().isInstance(event)) {
                continue;
            }
            try {
                registration.accept(event);
                delivered++;
            } catch (RuntimeException exception) {
                LOGGER.warn(
                        "Optional Totem event subscriber failed for {} contract v{}",
                        event.getClass().getName(),
                        event.contractVersion(),
                        exception
                );
            }
        }
        return delivered;
    }

    @FunctionalInterface
    public interface Subscription extends AutoCloseable {
        @Override
        void close();
    }

    private record ListenerRegistration<E extends TotemEvent>(
            Class<E> eventType,
            Consumer<? super E> listener
    ) {
        private void accept(TotemEvent event) {
            listener.accept(eventType.cast(event));
        }
    }
}
