package dev.totem.core.api.v1.manual;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/** Registers one independently owned chapter and a block that records it into the shared manual. */
public final class TotemModuleManualSource {
    private TotemModuleManualSource() {
    }

    public static void register(
            TotemManualSection section,
            Identifier advancementId,
            Predicate<BlockState> source
    ) {
        register(List.of(section), advancementId, source);
    }

    public static void register(
            List<TotemManualSection> sections,
            Identifier advancementId,
            Predicate<BlockState> source
    ) {
        List<TotemManualSection> copy = List.copyOf(sections);
        if (copy.isEmpty()) {
            throw new IllegalArgumentException("A module manual source requires at least one section");
        }
        Objects.requireNonNull(advancementId, "advancementId");
        Objects.requireNonNull(source, "source");
        copy.forEach(TotemManualRegistry.global()::register);
        TotemManualLifecycle.registerLoginRefresh();

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.isSpectator() || !source.test(world.getBlockState(hitResult.getBlockPos()))) {
                return InteractionResult.PASS;
            }
            ItemStack active = player.getItemInHand(hand);
            if (!TotemManualPlayerHelper.supportsSourceInteraction(active, ignored -> false)) {
                return InteractionResult.PASS;
            }
            if (world.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            return TotemManualChapterRecorder.acquireSections(
                    (ServerPlayer) player,
                    hand,
                    copy,
                    advancementId,
                    ignored -> false
            ).handled() ? InteractionResult.SUCCESS : InteractionResult.PASS;
        });
    }
}
