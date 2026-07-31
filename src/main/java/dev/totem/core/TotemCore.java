package dev.totem.core;

import dev.totem.core.network.ExactModuleVersionGate;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Library-only mod initializer.  Gameplay registration is intentionally forbidden. */
public final class TotemCore implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("TotemCore");

    @Override
    public void onInitialize() {
        ExactModuleVersionGate.initializeServer();
        LOGGER.info("TotemCore API {}.{} initialized without gameplay registration", 1, 0);
    }
}
