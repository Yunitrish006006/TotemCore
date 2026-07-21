package dev.totem.core.api.v1.death;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Optional version-1 lifecycle contract between a death-backpack feature and a
 * location-marker feature. Neither feature is required by Core.
 */
public interface DeathBackpackNodeLifecycle {
    UUID create(ServerPlayer owner, ServerLevel level, BlockPos position);

    void rollback(ServerPlayer owner, ServerLevel level, UUID nodeId);

    boolean recover(ServerPlayer recoveringPlayer, UUID nodeId);
}
