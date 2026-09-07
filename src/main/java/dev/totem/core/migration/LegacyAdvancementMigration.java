package dev.totem.core.migration;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * One-way advancement progress migration for the namespace split.
 *
 * <p>Legacy definitions remain present for one compatibility cycle so a
 * player's stored {@code deadrecall:*} progress can be read. On join, every
 * completed legacy criterion with an identically named canonical counterpart
 * is awarded to {@code totem:*}. New gameplay awards canonical IDs only.</p>
 */
public final class LegacyAdvancementMigration {
    private static final String LEGACY_NAMESPACE = "deadrecall";
    private static final String CANONICAL_NAMESPACE = "totem";
    private static final Map<String, String> EXPLICIT_TARGET_PATHS = Map.of(
            "alchemy_root", "alchemy/alchemy_root",
            "alchemy_manual", "alchemy/alchemy_manual",
            "pig_manure_got_hit", "alchemy/pig_manure_got_hit",
            "pig_manure_hit_entity", "alchemy/pig_manure_hit_entity",
            "recipes/stone_bowl", "alchemy/recipes/stone_bowl",
            "stone_bowl", "alchemy/stone_bowl"
    );
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();

    private LegacyAdvancementMigration() {
    }

    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }
        ServerPlayConnectionEvents.JOIN.register((listener, sender, server) ->
                migrate(listener.getPlayer()));
    }

    static Identifier canonicalTarget(Identifier legacyId) {
        if (legacyId == null || !LEGACY_NAMESPACE.equals(legacyId.getNamespace())) {
            return null;
        }
        return Identifier.fromNamespaceAndPath(
                CANONICAL_NAMESPACE,
                EXPLICIT_TARGET_PATHS.getOrDefault(legacyId.getPath(), legacyId.getPath())
        );
    }

    static int migrate(ServerPlayer player) {
        if (player == null || player.level().getServer() == null) {
            return 0;
        }

        var advancements = player.level().getServer().getAdvancements();
        int migratedCriteria = 0;
        for (AdvancementHolder legacy : advancements.getAllAdvancements()) {
            Identifier targetId = canonicalTarget(legacy.id());
            if (targetId == null) {
                continue;
            }
            AdvancementHolder canonical = advancements.get(targetId);
            if (canonical == null) {
                continue;
            }

            AdvancementProgress legacyProgress = player.getAdvancements().getOrStartProgress(legacy);
            if (!legacyProgress.hasProgress()) {
                continue;
            }
            for (String criterion : legacyProgress.getCompletedCriteria()) {
                if (canonical.value().criteria().containsKey(criterion)
                        && player.getAdvancements().award(canonical, criterion)) {
                    migratedCriteria++;
                }
            }
        }
        return migratedCriteria;
    }
}
