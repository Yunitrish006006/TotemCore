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
 * Process-local registry for compatibility bundles that translate legacy item
 * identifiers to their canonical owner identifiers.
 *
 * <p>Core owns only the migration mechanism. The compatibility bundle owns
 * every concrete mapping and the placeholder registrations required to decode
 * old saves. Feature modules can therefore remain standalone and ask this API
 * whether a stack belongs to one of their canonical item families.</p>
 */
public final class LegacyItemMigrationRegistry {
    private static final Map<Identifier, Target> MAPPINGS = new LinkedHashMap<>();

    private LegacyItemMigrationRegistry() {
    }

    public static synchronized void register(Identifier legacyId, Identifier canonicalId) {
        Item canonicalItem = BuiltInRegistries.ITEM.containsKey(canonicalId)
                ? BuiltInRegistries.ITEM.getValue(canonicalId)
                : null;
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
        Target existing = MAPPINGS.putIfAbsent(legacyId, target);
        if (existing != null && !existing.equals(target)) {
            throw new IllegalStateException(
                    "Legacy item ID " + legacyId + " is already mapped to " + existing.id()
            );
        }
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
        if (target == null || target.item() == stack.getItem()) {
            return stack;
        }
        return stack.transmuteCopy(target.item(), stack.getCount());
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
            return target != null && target.item() == canonicalItem;
        }
    }

    public static synchronized Map<Identifier, Identifier> snapshot() {
        Map<Identifier, Identifier> snapshot = new LinkedHashMap<>();
        MAPPINGS.forEach((legacyId, target) -> snapshot.put(legacyId, target.id()));
        return Collections.unmodifiableMap(snapshot);
    }

    private record Target(Identifier id, Item item) {
    }
}
