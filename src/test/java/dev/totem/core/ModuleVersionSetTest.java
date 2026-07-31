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
        client.remove("totem-nexus");

        assertEquals(
                java.util.List.of(
                        new ModuleVersionSet.Mismatch("totem-remnant", "0.1.7", "0.1.6"),
                        new ModuleVersionSet.Mismatch("totem-nexus", "0.2.2", ModuleVersionSet.MISSING)
                ),
                ModuleVersionSet.compare(server, client)
        );
    }

    private static Map<String, String> completeVersions() {
        Map<String, String> versions = new LinkedHashMap<>();
        versions.put("deadrecall", "2.4.7");
        versions.put("totem-core", "0.4.0");
        versions.put("totem-remnant", "0.1.7");
        versions.put("totem-discord-bridge", "0.1.4");
        versions.put("totem-automata", "0.1.9");
        versions.put("totem-alchemy", "0.1.7");
        versions.put("totem-enchanting", "0.1.3");
        versions.put("totem-vanilla-tweaks", "0.1.5");
        versions.put("totem-nexus", "0.2.2");
        return versions;
    }
}
