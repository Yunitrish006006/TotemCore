package dev.totem.core.api.v1.migration;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Process-local registry that translates legacy item identifiers to their
 * canonical Totem owner identifiers.
 *
 * <p>The permanent DeadRecall compatibility aliases are seeded when this class
 * is initialized. They therefore exist independently of Fabric entrypoint
 * ordering. Feature modules may still register or resolve additional mappings,
 * and canonical items remain lazily resolved when a stack is actually used.</p>
 */
public final class LegacyItemMigrationRegistry {
    private static final Map<Identifier, Target> MAPPINGS = createInitialMappings();

    private LegacyItemMigrationRegistry() {
    }

    private static Map<Identifier, Target> createInitialMappings() {
        Map<Identifier, Target> mappings = new LinkedHashMap<>();
        putBuiltIn(mappings, "backpack_basic", "remnant/backpack_basic");
        putBuiltIn(mappings, "backpack_standard", "remnant/backpack_standard");
        putBuiltIn(mappings, "backpack_advanced", "remnant/backpack_advanced");
        putBuiltIn(mappings, "backpack_netherite", "remnant/backpack_netherite");
        putBuiltIn(mappings, "death_backpack", "remnant/death_backpack");
        putBuiltIn(mappings, "copper_wrench", "automata/copper_wrench");
        putBuiltIn(mappings, "saltpeter", "alchemy/saltpeter");
        putBuiltIn(mappings, "pig_manure", "alchemy/pig_manure");
        putBuiltIn(mappings, "wood_ash", "alchemy/wood_ash");
        putBuiltIn(mappings, "cocoa_powder", "alchemy/cocoa_powder");
        putBuiltIn(mappings, "hot_cocoa", "alchemy/hot_cocoa");
        putBuiltIn(mappings, "cherry_brew", "alchemy/cherry_brew");
        putBuiltIn(mappings, "stone_bowl", "alchemy/stone_bowl");
        putBuiltIn(mappings, "sulfur_bowl", "alchemy/sulfur_bowl");
        return mappings;
    }

    private static void putBuiltIn(Map<Identifier, Target> mappings, String legacyPath, String canonicalPath) {
        Identifier legacyId = Identifier.fromNamespaceAndPath("deadrecall", legacyPath);
        Identifier canonicalId = Identifier.fromNamespaceAndPath("totem", canonicalPath);
        mappings.put(legacyId, new Target(canonicalId, null));
    }

    public static synchronized void registerDeferred(Identifier legacyId, Identifier canonicalId) {
        Objects.requireNonNull(legacyId, "legacyId");
        Objects.requireNonNull(canonicalId, "canonicalId");
        if (legacyId.equals(canonicalId)) {
            throw new IllegalArgumentException("Legacy and canonical item IDs must differ: " + legacyId);
        }

        Target target = new Target(canonicalId, null);
        Target existing = MAPPINGS.putIfAbsent(legacyId, target);
        if (existing != null && !existing.id().equals(canonicalId)) {
            throw new IllegalStateException(
                    "Legacy item ID " + legacyId + " is already mapped to " + existing.id()
            );
        }
    }

    public static synchronized void register(Identifier legacyId, Identifier canonicalId) {
        Item canonicalItem = resolveRegisteredItem(canonicalId);
        if (canonicalItem == null) {
            throw new IllegalStateException(
                    "Canonical item must be registered before its migration: " + canonicalId
            );
        }
        register(legacyId, canonicalId, canonicalItem);
    }

    public static synchronized void register(
            Identifier legacyId,
            Identifier canonicalId,
            Item canonicalItem
    ) {
        Objects.requireNonNull(legacyId, "legacyId");
        Objects.requireNonNull(canonicalId, "canonicalId");
        Objects.requireNonNull(canonicalItem, "canonicalItem");
        if (legacyId.equals(canonicalId)) {
            throw new IllegalArgumentException("Legacy and canonical item IDs must differ: " + legacyId);
        }

        Target target = new Target(canonicalId, canonicalItem);
        Target existing = MAPPINGS.get(legacyId);
        if (existing != null && !existing.id().equals(canonicalId)) {
            throw new IllegalStateException(
                    "Legacy item ID " + legacyId + " is already mapped to " + existing.id()
            );
        }
        MAPPINGS.put(legacyId, target);
    }

    public static ItemStack migrate(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return stack;
        }

        Identifier legacyId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        Target target;
        synchronized (LegacyItemMigrationRegistry.class) {
            target = MAPPINGS.get(legacyId);
        }
        if (target == null) {
            return stack;
        }

        Item canonicalItem = target.item() != null ? target.item() : resolveRegisteredItem(target.id());
        if (canonicalItem == null || canonicalItem == stack.getItem()) {
            return stack;
        }
        return stack.transmuteCopy(canonicalItem, stack.getCount());
    }

    public static boolean matches(ItemStack stack, Item canonicalItem) {
        if (stack == null || stack.isEmpty() || canonicalItem == null) {
            return false;
        }
        if (stack.is(canonicalItem)) {
            return true;
        }

        Identifier stackId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        synchronized (LegacyItemMigrationRegistry.class) {
            Target target = MAPPINGS.get(stackId);
            return target != null && target.id().equals(BuiltInRegistries.ITEM.getKey(canonicalItem));
        }
    }

    public static synchronized Map<Identifier, Identifier> snapshot() {
        Map<Identifier, Identifier> snapshot = new LinkedHashMap<>();
        MAPPINGS.forEach((legacyId, target) -> snapshot.put(legacyId, target.id()));
        return Collections.unmodifiableMap(snapshot);
    }

    private static Item resolveRegisteredItem(Identifier id) {
        if (!BuiltInRegistries.ITEM.containsKey(id)) {
            return null;
        }
        return BuiltInRegistries.ITEM.getValue(id);
    }

    private record Target(Identifier id, Item item) {
    }
}
