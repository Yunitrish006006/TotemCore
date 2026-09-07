package dev.totem.core.migration;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LegacyAdvancementMigrationTest {
    @Test
    void mapsOnlyLegacyNamespaceToTheMatchingCanonicalPath() {
        assertEquals(
                Identifier.fromNamespaceAndPath("totem", "locksmith/locked_network"),
                LegacyAdvancementMigration.canonicalTarget(
                        Identifier.fromNamespaceAndPath("deadrecall", "locksmith/locked_network"))
        );
        assertNull(LegacyAdvancementMigration.canonicalTarget(
                Identifier.fromNamespaceAndPath("minecraft", "story/root")));
        assertNull(LegacyAdvancementMigration.canonicalTarget(null));
    }

    @Test
    void mapsRelocatedAlchemyAdvancementsToTheirModuleOwnedPaths() {
        assertEquals(
                Identifier.fromNamespaceAndPath("totem", "alchemy/recipes/stone_bowl"),
                LegacyAdvancementMigration.canonicalTarget(
                        Identifier.fromNamespaceAndPath("deadrecall", "recipes/stone_bowl"))
        );
    }
}
