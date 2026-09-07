package dev.totem.core.api.v1.migration;

import net.minecraft.nbt.CompoundTag;

/**
 * Rewrites one feature's legacy identifiers in raw persisted NBT before
 * Minecraft decodes registry-backed values from it.
 *
 * <p>Implementations must be narrowly allow-listed: they may rewrite only
 * identifiers owned by that feature and must leave unrelated legacy data
 * untouched. Returning {@code true} records that the tag was changed.</p>
 */
@FunctionalInterface
public interface LegacyNbtMigration {
    boolean migrate(CompoundTag root);
}
