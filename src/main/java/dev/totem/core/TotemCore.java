package dev.totem.core;

import dev.totem.core.api.v1.manual.TotemManualOnboarding;
import dev.totem.core.network.ExactModuleVersionGate;
import dev.totem.core.network.TotemManualPayloadRegistration;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shared Totem API and first-step manual initializer. */
public final class TotemCore implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("TotemCore");

    @Override
    public void onInitialize() {
        ExactModuleVersionGate.initializeServer();
        TotemManualPayloadRegistration.register();
        TotemManualOnboarding.register();
        LOGGER.info("TotemCore API {}.{} initialized with guided manual onboarding", 1, 1);
    }
}
