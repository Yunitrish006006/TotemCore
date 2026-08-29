package dev.totem.core.api.v1.client.observer;

/**
 * Capability marker implemented by a production Screen that supports a
 * read-only Observer mode. The same Screen class may return {@code false} in
 * normal local use. Coordinators use this contract for rebroadcast suppression
 * instead of maintaining a list of concrete Screen classes.
 */
public interface ObserverReadOnlyScreen {
    /** Returns whether this Screen instance is currently in read-only Observer mode. */
    default boolean totem$isObserverReadOnly() {
        return true;
    }
}
