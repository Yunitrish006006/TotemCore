package dev.totem.core;

import dev.totem.core.network.ModuleVersionSet;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleVersionSetTest {
    @Test
    void acceptsOnlyWhenEveryTrackedModuleVersionMatches() {
        Map<String, String> server = completeVersions();
        assertTrue(ModuleVersionSet.compare(server, new LinkedHashMap<>(server)).isEmpty());
    }

    @Test
    void reportsEachDifferentOrMissingModuleSeparately() {
        Map<String, String> server = completeVersions();
        Map<String, String> client = new LinkedHashMap<>(server);
        client.put("totem-remnant", "0.1.6");
        client.put("totem-excavation", "0.1.0");
        client.put("totem-locksmith", "0.0.9");
        client.remove("totem-nexus");
        client.remove("totem-villagers");

        assertEquals(
                java.util.List.of(
                        new ModuleVersionSet.Mismatch("totem-remnant", "0.2.11", "0.1.6"),
                        new ModuleVersionSet.Mismatch("totem-excavation", "0.1.2", "0.1.0"),
                        new ModuleVersionSet.Mismatch("totem-locksmith", "0.1.0", "0.0.9"),
                        new ModuleVersionSet.Mismatch("totem-nexus", "0.2.6", ModuleVersionSet.MISSING),
                        new ModuleVersionSet.Mismatch("totem-villagers", "0.1.23", ModuleVersionSet.MISSING)
                ),
                ModuleVersionSet.compare(server, client)
        );
    }

    private static Map<String, String> completeVersions() {
        Map<String, String> versions = new LinkedHashMap<>();
        versions.put("deadrecall", "2.4.11");
        versions.put("totem-core", "0.6.0");
        versions.put("totem-remnant", "0.2.11");
        versions.put("totem-discord-bridge", "0.1.6");
        versions.put("totem-automata", "0.1.12");
        versions.put("totem-alchemy", "0.1.23");
        versions.put("totem-enchanting", "0.1.5");
        versions.put("totem-excavation", "0.1.2");
        versions.put("totem-locksmith", "0.1.0");
        versions.put("totem-vanilla-tweaks", "0.1.8");
        versions.put("totem-nexus", "0.2.6");
        versions.put("totem-villagers", "0.1.23");
        return versions;
    }
}
