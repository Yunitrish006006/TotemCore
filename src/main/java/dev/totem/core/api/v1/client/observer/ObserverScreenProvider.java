package dev.totem.core.api.v1.client.observer;

import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;
import java.util.Set;

/**
 * Fabric client entrypoint implemented by the module that owns a Screen.
 * Provider methods are invoked only on the Minecraft client thread.
 */
public interface ObserverScreenProvider {
    String ENTRYPOINT = "totem:observer_screen_provider";

    String familyId();
    int protocolVersion();
    Set<String> variants();
    ObserverScreenHandle create(ObserverScreenContext context, ObserverScreenSnapshot initialSnapshot);

    /** Captures state only when this provider owns the target production Screen. */
    default Optional<ObserverScreenSnapshot> capture(Screen screen, long sequence) {
        return Optional.empty();
    }

    default boolean supports(ObserverScreenSnapshot snapshot) {
        return snapshot != null
                && familyId().equals(snapshot.familyId())
                && protocolVersion() == snapshot.protocolVersion()
                && variants().contains(snapshot.variant());
    }
}
