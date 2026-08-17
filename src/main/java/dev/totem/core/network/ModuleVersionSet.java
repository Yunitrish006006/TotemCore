package dev.totem.core.network;

import net.fabricmc.loader.api.FabricLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The exact, ordered DeadRecall module graph exchanged during configuration. */
public final class ModuleVersionSet {
    public static final String MISSING = "<missing>";

    public static final List<String> MODULE_IDS = List.of(
            "deadrecall",
            "totem-core",
            "totem-remnant",
            "totem-discord-bridge",
            "totem-automata",
            "totem-alchemy",
            "totem-enchanting",
            "totem-excavation",
            "totem-locksmith",
            "totem-vanilla-tweaks",
            "totem-nexus",
            "totem-villagers"
    );

    private ModuleVersionSet() {
    }

    public static Map<String, String> loaded() {
        FabricLoader loader = FabricLoader.getInstance();
        LinkedHashMap<String, String> versions = new LinkedHashMap<>();
        for (String modId : MODULE_IDS) {
            String version = loader.getModContainer(modId)
                    .map(container -> container.getMetadata().getVersion().getFriendlyString())
                    .orElse(MISSING);
            versions.put(modId, version);
        }
        return immutableOrderedCopy(versions);
    }

    public static List<Mismatch> compare(
            Map<String, String> serverVersions,
            Map<String, String> clientVersions
    ) {
        List<Mismatch> mismatches = new ArrayList<>();
        for (String modId : MODULE_IDS) {
            String serverVersion = serverVersions.getOrDefault(modId, MISSING);
            String clientVersion = clientVersions.getOrDefault(modId, MISSING);
            if (!serverVersion.equals(clientVersion)) {
                mismatches.add(new Mismatch(modId, serverVersion, clientVersion));
            }
        }
        return List.copyOf(mismatches);
    }

    public static Map<String, String> immutableOrderedCopy(Map<String, String> versions) {
        LinkedHashMap<String, String> copy = new LinkedHashMap<>();
        for (String modId : MODULE_IDS) {
            copy.put(modId, versions.getOrDefault(modId, MISSING));
        }
        return Collections.unmodifiableMap(copy);
    }

    public record Mismatch(String modId, String serverVersion, String clientVersion) {
    }
}
