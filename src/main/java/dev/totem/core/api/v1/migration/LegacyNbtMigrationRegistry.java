package dev.totem.core.api.v1.migration;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Shared pre-decode migration seam for feature-owned legacy world and player
 * data.
 *
 * <p>Core invokes this registry while data is still raw NBT, before chunk,
 * player, block-entity, potion, or effect codecs consult a registry. This is
 * deliberately not a registry-alias API: a successful rewrite lets the next
 * normal save persist only canonical identifiers.</p>
 */
public final class LegacyNbtMigrationRegistry {
    private static final List<LegacyNbtMigration> MIGRATIONS = new ArrayList<>();

    private LegacyNbtMigrationRegistry() {
    }

    /** Registers an idempotent, allow-listed raw-NBT migration. */
    public static synchronized void register(LegacyNbtMigration migration) {
        Objects.requireNonNull(migration, "migration");
        if (!MIGRATIONS.contains(migration)) {
            MIGRATIONS.add(migration);
        }
    }

    /**
     * Applies every registered migration in registration order.
     *
     * <p>This method is safe to call before feature entrypoints have finished
     * registering; it simply performs no work until a feature contributes a
     * migration.</p>
     */
    public static boolean migrate(CompoundTag root) {
        if (root == null) {
            return false;
        }

        List<LegacyNbtMigration> snapshot;
        synchronized (LegacyNbtMigrationRegistry.class) {
            snapshot = List.copyOf(MIGRATIONS);
        }

        boolean changed = false;
        for (LegacyNbtMigration migration : snapshot) {
            changed |= migration.migrate(root);
        }
        return changed;
    }
}
