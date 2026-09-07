package dev.totem.core.client;

import net.fabricmc.api.ClientModInitializer;

/** Registers Core client features. */
public final class TotemCoreClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TotemStarterManualOverlay.register();
    }
}
