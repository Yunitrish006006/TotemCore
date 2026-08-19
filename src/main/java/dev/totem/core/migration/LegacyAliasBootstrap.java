package dev.totem.core.migration;

import dev.totem.core.api.v1.migration.LegacyItemMigrationRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.List;

/**
 * Permanent decode authority for the legacy DeadRecall item namespace.
 *
 * <p>These placeholders make old saves safe to open with TotemCore alone.
 * Canonical feature items are resolved lazily, so a legacy stack remains intact
 * if its owning feature module is temporarily absent and migrates once that
 * module is installed again.</p>
 */
public final class LegacyAliasBootstrap {
    private static final List<Alias> ALIASES = List.of(
            alias("backpack_basic", "remnant/backpack_basic", 1, false),
            alias("backpack_standard", "remnant/backpack_standard", 1, false),
            alias("backpack_advanced", "remnant/backpack_advanced", 1, false),
            alias("backpack_netherite", "remnant/backpack_netherite", 1, true),
            alias("death_backpack", "remnant/death_backpack", 1, true),
            alias("copper_wrench", "automata/copper_wrench", 1, false),
            alias("saltpeter", "alchemy/saltpeter", 64, false),
            alias("pig_manure", "alchemy/pig_manure", 64, false),
            alias("wood_ash", "alchemy/wood_ash", 64, false),
            alias("cocoa_powder", "alchemy/cocoa_powder", 1, false),
            alias("hot_cocoa", "alchemy/hot_cocoa", 16, false),
            alias("cherry_brew", "alchemy/cherry_brew", 16, false),
            alias("stone_bowl", "alchemy/stone_bowl", 1, false),
            alias("sulfur_bowl", "alchemy/sulfur_bowl", 1, false)
    );

    private static boolean registered;

    private LegacyAliasBootstrap() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        for (Alias alias : ALIASES) {
            LegacyItemMigrationRegistry.registerDeferred(alias.legacyId(), alias.canonicalId());
            registerPlaceholder(alias);
        }
        registered = true;
    }

    public static int aliasCount() {
        return ALIASES.size();
    }

    private static void registerPlaceholder(Alias alias) {
        if (BuiltInRegistries.ITEM.containsKey(alias.legacyId())) {
            throw new IllegalStateException(
                    "Legacy DeadRecall alias is already registered by another mod: " + alias.legacyId()
            );
        }
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, alias.legacyId());
        Item.Properties properties = new Item.Properties().setId(key).stacksTo(alias.maxStackSize());
        if (alias.fireResistant()) {
            properties.fireResistant();
        }
        Registry.register(BuiltInRegistries.ITEM, alias.legacyId(), new LegacyAliasItem(properties));
    }

    private static Alias alias(String legacyPath, String canonicalPath, int maxStackSize, boolean fireResistant) {
        return new Alias(
                Identifier.fromNamespaceAndPath("deadrecall", legacyPath),
                Identifier.fromNamespaceAndPath("totem", canonicalPath),
                maxStackSize,
                fireResistant
        );
    }

    private record Alias(Identifier legacyId, Identifier canonicalId, int maxStackSize, boolean fireResistant) {
    }
}
