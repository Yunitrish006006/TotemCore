package dev.totem.core.api.v1.death;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;

/**
 * Optional feature-owned policy for one item that may survive a player death.
 *
 * <p>The policy only authorizes retention. The death-owning module remains
 * responsible for extracting, persisting and restoring the item exactly once.</p>
 */
@FunctionalInterface
public interface DeathRetainedItemPolicy {
    boolean shouldRetain(ServerPlayer player, ItemStack stack);

    static void register(DeathRetainedItemPolicy policy) {
        Holder.policy = Objects.requireNonNull(policy, "policy");
    }

    static Optional<DeathRetainedItemPolicy> current() {
        return Optional.ofNullable(Holder.policy);
    }

    final class Holder {
        private static volatile DeathRetainedItemPolicy policy;

        private Holder() {
        }
    }
}
