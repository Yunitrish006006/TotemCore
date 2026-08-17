package dev.totem.core.api.v1.manual;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Optional lifecycle hook activated by feature modules after section
 * registration. Core itself does not activate gameplay callbacks.
 */
public final class TotemManualLifecycle {
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();

    private TotemManualLifecycle() {
    }

    public static void registerLoginRefresh() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }
        ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> {
            TotemManualPlayerHelper.ensureBasicManual(listener.getPlayer());
            TotemManualPlayerHelper.refreshInventory(listener.getPlayer());
        });
    }
}
