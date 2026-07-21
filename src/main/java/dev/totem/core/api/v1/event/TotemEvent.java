package dev.totem.core.api.v1.event;

/** Marker interface for immutable, versioned cross-feature lifecycle events. */
public interface TotemEvent {
    int contractVersion();
}
