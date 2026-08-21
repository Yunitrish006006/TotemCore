package dev.totem.core.api.v1.manual;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/** Server-owned acquisition, migration, consolidation and inventory refresh operations. */
public final class TotemManualPlayerHelper {
    private static final Identifier KNOWLEDGE_ADVANCEMENT =
            Identifier.fromNamespaceAndPath("deadrecall", "knowledge_is_power");
    private static final String KNOWLEDGE_CRITERION = "has_manual";
    private static final Identifier BASIC_ADVANCEMENT =
            Identifier.fromNamespaceAndPath("deadrecall", "root");
    private static final String BASIC_CRITERION = "received_basic_manual";

    private TotemManualPlayerHelper() {
    }

    public static boolean supportsSourceInteraction(
            ItemStack activeStack,
            Predicate<ItemStack> legacyRecognizer
    ) {
        Objects.requireNonNull(legacyRecognizer, "legacyRecognizer");
        return activeStack != null && (activeStack.is(Items.BOOK)
                || TotemManualAssembler.isCanonical(activeStack)
                || legacyRecognizer.test(activeStack));
    }

    public static Result acquire(
            ServerPlayer player,
            InteractionHand activeHand,
            Predicate<ItemStack> legacyRecognizer
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(activeHand, "activeHand");
        Objects.requireNonNull(legacyRecognizer, "legacyRecognizer");

        InteractionHand otherHand = activeHand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        ItemStack active = player.getItemInHand(activeHand);
        ItemStack other = player.getItemInHand(otherHand);
        boolean activeManual = recognized(active, legacyRecognizer);
        boolean otherManual = recognized(other, legacyRecognizer);

        Result result;
        if (activeManual && otherManual) {
            boolean migrated = !TotemManualAssembler.isCanonical(active);
            TotemManualAssembler.rebuild(active, mergedSections(
                    sectionsOf(active, legacyRecognizer),
                    sectionsOf(other, legacyRecognizer)
            ));
            other.shrink(1);
            result = migrated ? Result.MIGRATED_AND_CONSOLIDATED : Result.CONSOLIDATED;
        } else if (activeManual) {
            boolean migrated = !TotemManualAssembler.isCanonical(active);
            TotemManualAssembler.rebuild(active);
            result = migrated ? Result.MIGRATED : Result.REFRESHED;
        } else if (active.is(Items.BOOK) && otherManual) {
            boolean migrated = !TotemManualAssembler.isCanonical(other);
            TotemManualAssembler.rebuild(other);
            result = migrated ? Result.MIGRATED : Result.REFRESHED;
        } else if (active.is(Items.BOOK)) {
            ItemStack manual = TotemManualAssembler.create();
            if (active.getCount() == 1) {
                player.setItemInHand(activeHand, manual);
            } else {
                active.shrink(1);
                insertOrDrop(player, manual);
            }
            result = Result.CREATED;
        } else {
            return Result.PASS;
        }

        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.BOOK_PAGE_TURN,
                SoundSource.PLAYERS,
                0.8F,
                1.0F
        );
        awardKnowledgeAdvancement(player);
        player.sendSystemMessage(Component.translatable(result.messageKey()));
        return result;
    }

    /**
     * Creates or refreshes one module-scoped guide without consuming a marked
     * guide used as the recording reference.
     */
    public static Result acquireSections(
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
        List<TotemManualSection> sections = suppliedSections.stream().sorted().toList();
        if (sections.isEmpty()) {
            throw new IllegalArgumentException("A module guide requires at least one section");
        }

        ItemStack active = player.getItemInHand(activeHand);
        if (!supportsSourceInteraction(active, legacyRecognizer)) {
            return Result.PASS;
        }
        InteractionHand otherHand = activeHand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        ItemStack other = player.getItemInHand(otherHand);

        boolean activeLegacy = legacyRecognizer.test(active);
        boolean activeExactTarget = isExactGuide(active, sections);
        boolean otherLegacy = legacyRecognizer.test(other);
        boolean otherExactTarget = isExactGuide(other, sections);
        ItemStack existing = findExactGuide(player, sections);
        ModuleGuideAction action = moduleGuideAction(
                activeLegacy,
                activeExactTarget,
                otherLegacy,
                otherExactTarget,
                existing != null,
                active.is(Items.BOOK),
                TotemManualAssembler.isCanonical(active)
        );

        Result result = Result.PASS;
        switch (action) {
            case CONSOLIDATE_ACTIVE -> {
                TotemManualAssembler.rebuild(active, sections);
                other.shrink(1);
                result = activeLegacy || otherLegacy
                        ? Result.MIGRATED_AND_CONSOLIDATED
                        : Result.CONSOLIDATED;
            }
            case MIGRATE_ACTIVE -> {
                TotemManualAssembler.rebuild(active, sections);
                result = Result.MIGRATED;
            }
            case REFRESH_ACTIVE -> {
                TotemManualAssembler.rebuild(active, sections);
                result = Result.REFRESHED;
            }
            case REFRESH_EXISTING -> {
                TotemManualAssembler.rebuild(existing, sections);
                result = Result.REFRESHED;
            }
            case MIGRATE_OTHER -> {
                TotemManualAssembler.rebuild(other, sections);
                result = Result.MIGRATED;
            }
            case CREATE_FROM_BOOK -> {
                ItemStack guide = TotemManualAssembler.create(sections);
                if (active.getCount() == 1) {
                    player.setItemInHand(activeHand, guide);
                } else {
                    active.shrink(1);
                    insertOrDrop(player, guide);
                }
                result = Result.CREATED;
            }
            case CREATE_FROM_REFERENCE -> {
                // A canonical guide is a reusable recording reference. Keep it
                // untouched and deliver a separate target-only module guide.
                insertOrDrop(player, TotemManualAssembler.create(sections));
                result = Result.CREATED;
            }
            case PASS -> {
                return Result.PASS;
            }
        }

        playPageSound(player, 1.0F);
        awardAdvancement(player, advancementId, "has_manual");
        awardKnowledgeAdvancement(player);
        player.sendSystemMessage(Component.translatable(result.messageKey()));
        return result;
    }

    /** Gives the Core getting-started guide exactly once according to root progress. */
    public static boolean ensureBasicManual(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        var advancement = player.level().getServer().getAdvancements().get(BASIC_ADVANCEMENT);
        if (advancement == null || player.getAdvancements().getOrStartProgress(advancement).isDone()) {
            return false;
        }

        if (findExactGuide(player, List.of(TotemManualOnboarding.SECTION)) == null) {
            insertOrDrop(player, TotemManualAssembler.create(List.of(TotemManualOnboarding.SECTION)));
            playPageSound(player, 1.1F);
            player.sendSystemMessage(Component.translatable("message.totem.manual.basic_received"));
        }
        player.getAdvancements().award(advancement, BASIC_CRITERION);
        return true;
    }

    /** Splits the canonical manual held by the player into one book per installed chapter. */
    public static int splitHeldManual(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        ItemStack manual = heldCanonicalManual(player);
        if (manual == null) {
            player.sendSystemMessage(Component.translatable("message.totem.manual.split_missing"));
            return 0;
        }

        List<TotemManualSection> sections = TotemManualAssembler.sections(manual);
        if (sections.size() <= 1) {
            player.sendSystemMessage(Component.translatable("message.totem.manual.split_unavailable"));
            return 0;
        }

        TotemManualAssembler.rebuild(manual, List.of(sections.getFirst()));
        for (int index = 1; index < sections.size(); index++) {
            ItemStack separated = TotemManualAssembler.create(List.of(sections.get(index)));
            insertOrDrop(player, separated);
        }
        playPageSound(player, 1.15F);
        player.sendSystemMessage(Component.translatable(
                "message.totem.manual.split_success",
                sections.size()
        ));
        return sections.size();
    }

    /** Refreshes only explicitly marked canonical manuals carried by this player. */
    public static int refreshInventory(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        int refreshed = 0;
        boolean carriesManual = false;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!TotemManualAssembler.isCanonical(stack)) {
                continue;
            }
            if (!isBasicGuide(stack)) {
                carriesManual = true;
            }
            if (!TotemManualAssembler.isCurrent(stack)) {
                TotemManualAssembler.rebuild(stack);
                refreshed++;
            }
        }
        if (carriesManual) {
            awardKnowledgeAdvancement(player);
        }
        return refreshed;
    }

    private static void awardKnowledgeAdvancement(ServerPlayer player) {
        awardAdvancement(player, KNOWLEDGE_ADVANCEMENT, KNOWLEDGE_CRITERION);
    }

    private static void awardAdvancement(
            ServerPlayer player,
            Identifier advancementId,
            String criterion
    ) {
        var advancement = player.level().getServer().getAdvancements().get(advancementId);
        if (advancement != null) {
            player.getAdvancements().award(advancement, criterion);
        }
    }

    private static ItemStack findExactGuide(
            ServerPlayer player,
            List<TotemManualSection> sections
    ) {
        List<Identifier> expected = sections.stream().sorted().map(TotemManualSection::id).toList();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (TotemManualAssembler.isCanonical(stack)
                    && TotemManualAssembler.sections(stack).stream()
                    .map(TotemManualSection::id).toList().equals(expected)) {
                return stack;
            }
        }
        return null;
    }

    static boolean isExactGuide(ItemStack stack, List<TotemManualSection> sections) {
        if (!TotemManualAssembler.isCanonical(stack)) return false;
        List<Identifier> expected = sections.stream().sorted()
                .map(TotemManualSection::id).toList();
        return TotemManualAssembler.sections(stack).stream()
                .map(TotemManualSection::id).toList().equals(expected);
    }

    static ModuleGuideAction moduleGuideAction(
            boolean activeLegacy,
            boolean activeExactTarget,
            boolean otherLegacy,
            boolean otherExactTarget,
            boolean existingExactTarget,
            boolean activeBook,
            boolean activeCanonicalReference
    ) {
        if ((activeLegacy || activeExactTarget) && (otherLegacy || otherExactTarget)) {
            return ModuleGuideAction.CONSOLIDATE_ACTIVE;
        }
        if (activeLegacy) return ModuleGuideAction.MIGRATE_ACTIVE;
        if (activeExactTarget) return ModuleGuideAction.REFRESH_ACTIVE;
        if (existingExactTarget) return ModuleGuideAction.REFRESH_EXISTING;
        if (otherLegacy) return ModuleGuideAction.MIGRATE_OTHER;
        if (activeBook) return ModuleGuideAction.CREATE_FROM_BOOK;
        if (activeCanonicalReference) return ModuleGuideAction.CREATE_FROM_REFERENCE;
        return ModuleGuideAction.PASS;
    }

    private static boolean isBasicGuide(ItemStack stack) {
        return TotemManualAssembler.sections(stack).stream()
                .map(TotemManualSection::id)
                .toList()
                .equals(List.of(TotemManualOnboarding.SECTION_ID));
    }

    private static void insertOrDrop(ServerPlayer player, ItemStack stack) {
        var inventory = player.getInventory();
        ManualDeliveryAction action = manualDeliveryAction(
                inventory.getFreeSlot(),
                inventory.getSlotWithRemainingSpace(stack)
        );
        if (action == ManualDeliveryAction.DROP) {
            player.drop(stack, false);
            return;
        }

        inventory.add(stack);
        if (!stack.isEmpty()) player.drop(stack, false);
    }

    static ManualDeliveryAction manualDeliveryAction(int freeSlot, int stackableSlot) {
        return freeSlot >= 0 || stackableSlot >= 0
                ? ManualDeliveryAction.INSERT
                : ManualDeliveryAction.DROP;
    }

    private static void playPageSound(ServerPlayer player, float pitch) {
        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.BOOK_PAGE_TURN,
                SoundSource.PLAYERS,
                0.8F,
                pitch
        );
    }

    private static boolean recognized(ItemStack stack, Predicate<ItemStack> legacyRecognizer) {
        return TotemManualAssembler.isCanonical(stack) || legacyRecognizer.test(stack);
    }

    private static ItemStack heldCanonicalManual(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (TotemManualAssembler.isCanonical(mainHand)) {
            return mainHand;
        }
        ItemStack offHand = player.getOffhandItem();
        return TotemManualAssembler.isCanonical(offHand) ? offHand : null;
    }

    private static List<TotemManualSection> sectionsOf(
            ItemStack stack,
            Predicate<ItemStack> legacyRecognizer
    ) {
        return TotemManualAssembler.isCanonical(stack)
                ? TotemManualAssembler.sections(stack)
                : legacyRecognizer.test(stack)
                ? TotemManualRegistry.global().sections()
                : List.of();
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

    public enum Result {
        PASS(null),
        CREATED("message.totem.manual.received"),
        REFRESHED("message.totem.manual.refreshed"),
        MIGRATED("message.totem.manual.migrated"),
        CONSOLIDATED("message.totem.manual.consolidated"),
        MIGRATED_AND_CONSOLIDATED("message.totem.manual.consolidated");

        private final String messageKey;

        Result(String messageKey) {
            this.messageKey = messageKey;
        }

        public boolean handled() {
            return this != PASS;
        }

        public String messageKey() {
            return messageKey;
        }
    }

    enum ModuleGuideAction {
        CONSOLIDATE_ACTIVE,
        MIGRATE_ACTIVE,
        REFRESH_ACTIVE,
        REFRESH_EXISTING,
        MIGRATE_OTHER,
        CREATE_FROM_BOOK,
        CREATE_FROM_REFERENCE,
        PASS
    }

    enum ManualDeliveryAction {
        INSERT,
        DROP
    }
}
