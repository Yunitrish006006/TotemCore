package dev.totem.core.api.v1.client.observer;

import net.fabricmc.loader.api.FabricLoader;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Lazy optional-module discovery; duplicate family ownership is rejected. */
public final class ObserverScreenProviders {
    private ObserverScreenProviders() {}

    public static Map<String, ObserverScreenProvider> discover() {
        Map<String, ObserverScreenProvider> providers = new LinkedHashMap<>();
        FabricLoader.getInstance().getEntrypointContainers(
                ObserverScreenProvider.ENTRYPOINT, ObserverScreenProvider.class).forEach(container -> {
            ObserverScreenProvider provider = container.getEntrypoint();
            ObserverScreenProvider previous = providers.putIfAbsent(provider.familyId(), provider);
            if (previous != null) {
                throw new IllegalStateException("Duplicate Observer screen provider for " + provider.familyId());
            }
        });
        return Map.copyOf(providers);
    }

    public static Optional<ObserverScreenProvider> compatible(
            Map<String, ObserverScreenProvider> providers, ObserverScreenSnapshot snapshot) {
        ObserverScreenProvider provider = providers.get(snapshot.familyId());
        return provider != null && provider.supports(snapshot) ? Optional.of(provider) : Optional.empty();
    }
}
