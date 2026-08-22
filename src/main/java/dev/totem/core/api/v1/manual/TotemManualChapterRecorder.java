package dev.totem.core.api.v1.manual;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/** Appends module-owned chapters to the player's existing shared Totem Manual. */
public final class TotemManualChapterRecorder {
    private static final Identifier KNOWLEDGE_ADVANCEMENT =
            Identifier.fromNamespaceAndPath("deadrecall", "knowledge_is_power");

    private TotemManualChapterRecorder() {
    }

    public static TotemManualPlayerHelper.Result acquireSections(
            ServerPlayer player,
            InteractionHand activeHand,
            List<TotemManualSection> suppliedSections,
            Identifier advancementId,
            Predicate<ItemStack> legacyRecognizer
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(activeHand, "activeHand");
        Objects.requireNonNull(suppliedSections, "suppliedSections");
        Objects.requireNonNull(advancementId, "advancementId");
        Objects.requireNonNull(legacyRecognizer, "legacyRecognizer");

        List<TotemManualSection> incoming = suppliedSections.stream().sorted().toList();
        if (incoming.isEmpty()) {
            throw new IllegalArgumentException("A manual source requires at least one section");
        }

        ItemStack active = player.getItemInHand(activeHand);
        if (!TotemManualPlayerHelper.supportsSourceInteraction(active, legacyRecognizer)) {
            return TotemManualPlayerHelper.Result.PASS;
        }

        ItemStack target = findCanonicalManual(player, activeHand);
        if (target == null) {
            // A module source must always produce the shared manual shape. Seed the Core onboarding
            // chapter first instead of creating another module-only guide.
            return TotemManualPlayerHelper.acquireSectionsLegacy(
                    player,
                    activeHand,
                    mergedSections(List.of(TotemManualOnboarding.SECTION), incoming),
                    advancementId,
                    legacyRecognizer
            );
        }

        List<TotemManualSection> before = TotemManualAssembler.sections(target);
        List<TotemManualSection> merged = mergedSections(before, incoming);
        boolean addedChapter = merged.size() > before.size();
        TotemManualAssembler.rebuild(target, merged);

        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.BOOK_PAGE_TURN,
                SoundSource.PLAYERS,
                0.8F,
                addedChapter ? 1.1F : 1.0F
        );
        award(player, advancementId, "has_manual");
        award(player, KNOWLEDGE_ADVANCEMENT, "has_manual");

        TotemManualPlayerHelper.Result result = addedChapter
                ? TotemManualPlayerHelper.Result.CONSOLIDATED
                : TotemManualPlayerHelper.Result.REFRESHED;
        player.sendSystemMessage(Component.translatable(result.messageKey()));
        return result;
    }

    static List<TotemManualSection> mergedSections(
            List<TotemManualSection> first,
            List<TotemManualSection> second
    ) {
        Map<Identifier, TotemManualSection> sections = new LinkedHashMap<>();
        first.forEach(section -> sections.put(section.id(), section));
        second.forEach(section -> sections.put(section.id(), section));
        return sections.values().stream().sorted().toList();
    }

    private static ItemStack findCanonicalManual(ServerPlayer player, InteractionHand activeHand) {
        ItemStack active = player.getItemInHand(activeHand);
        if (TotemManualAssembler.isCanonical(active)) {
            return active;
        }
        InteractionHand otherHand = activeHand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        ItemStack other = player.getItemInHand(otherHand);
        if (TotemManualAssembler.isCanonical(other)) {
            return other;
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (TotemManualAssembler.isCanonical(stack)) {
                return stack;
            }
        }
        return null;
    }

    private static void award(ServerPlayer player, Identifier advancementId, String criterion) {
        var advancement = player.level().getServer().getAdvancements().get(advancementId);
        if (advancement != null) {
            player.getAdvancements().award(advancement, criterion);
        }
    }
}
