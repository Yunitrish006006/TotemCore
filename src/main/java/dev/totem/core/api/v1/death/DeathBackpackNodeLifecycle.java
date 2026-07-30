package dev.totem.core.api.v1.death;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

/** Optional lifecycle adapter for a Remnant-owned death backpack location marker. */
public interface DeathBackpackNodeLifecycle {
    UUID create(ServerPlayer owner, ServerLevel level, BlockPos position);
    /**
     * Persists the reverse link after Remnant has created the backpack entity.
     *
     * <p>The default keeps older lifecycle providers source compatible. New
     * authorities should persist this UUID so diagnostics never need to load
     * chunks to search for backpack entities.</p>
     */
    default void bind(ServerLevel level, UUID nodeId, UUID backpackEntityId) { }
    void rollback(ServerPlayer owner, ServerLevel level, UUID nodeId);
    boolean recover(ServerPlayer recoveringPlayer, UUID nodeId);

    static void register(DeathBackpackNodeLifecycle adapter) { Holder.adapter = adapter; }
    static Optional<DeathBackpackNodeLifecycle> current() { return Optional.ofNullable(Holder.adapter); }

    final class Holder {
        private static volatile DeathBackpackNodeLifecycle adapter;
        private Holder() { }
    }
}
