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
 * <p>Mappings may be registered before the canonical feature module has
 * registered its item. This allows TotemCore to keep old save identifiers
 * decode-safe even when DeadRecall is no longer installed. The canonical item
 * is resolved lazily from the item registry when a stack is actually used.</p>
 */
public final class LegacyItemMigrationRegistry {
    private static final Map<Identifier, Target> MAPPINGS = new LinkedHashMap<>();

    private LegacyItemMigrationRegistry() {
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
