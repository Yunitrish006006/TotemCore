package dev.totem.core;

import net.fabricmc.api.ModInitializer;

/** Library-only mod initializer.  Gameplay registration is intentionally forbidden. */
public final class TotemCore implements ModInitializer {
    @Override
    public void onInitialize() {
        // Core exposes contracts only; feature modules own all gameplay registration.
    }
}
