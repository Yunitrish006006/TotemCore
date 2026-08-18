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
        client.put("totem-remnant", "0.2.11");
        client.put("totem-excavation", "0.1.2");
        client.put("totem-locksmith", "0.1.0");
        client.remove("totem-nexus");
        client.remove("totem-villagers");

        assertEquals(
                java.util.List.of(
                        new ModuleVersionSet.Mismatch("totem-remnant", "0.2.12", "0.2.11"),
                        new ModuleVersionSet.Mismatch("totem-excavation", "0.1.3", "0.1.2"),
                        new ModuleVersionSet.Mismatch("totem-locksmith", "0.1.1", "0.1.0"),
                        new ModuleVersionSet.Mismatch("totem-nexus", "0.2.7", ModuleVersionSet.MISSING),
                        new ModuleVersionSet.Mismatch("totem-villagers", "0.1.31", ModuleVersionSet.MISSING)
                ),
                ModuleVersionSet.compare(server, client)
        );
    }

    private static Map<String, String> completeVersions() {
        Map<String, String> versions = new LinkedHashMap<>();
        versions.put("deadrecall", "2.4.20");
        versions.put("totem-core", "0.6.1");
        versions.put("totem-remnant", "0.2.12");
        versions.put("totem-discord-bridge", "0.1.7");
        versions.put("totem-automata", "0.1.13");
        versions.put("totem-alchemy", "0.1.24");
        versions.put("totem-enchanting", "0.1.6");
        versions.put("totem-excavation", "0.1.3");
        versions.put("totem-locksmith", "0.1.1");
        versions.put("totem-vanilla-tweaks", "0.1.9");
        versions.put("totem-nexus", "0.2.7");
        versions.put("totem-villagers", "0.1.31");
        return versions;
    }
}
