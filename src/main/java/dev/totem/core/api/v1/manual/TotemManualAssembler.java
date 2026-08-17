package dev.totem.core.api.v1.manual;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.WrittenBookContent;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Stateless canonical vanilla written-book assembler.
 *
 * <p>Only the manual-owned name, written content and namespaced custom-data
 * fields are replaced. Other safe components on an existing stack survive a
 * refresh.</p>
 */
public final class TotemManualAssembler {
    public static final int SCHEMA_VERSION = 2;
    public static final int MAX_PAGES = 100;
    public static final String MANUAL_NAME_KEY = "item.totem.manual";
    public static final String COVER_PAGE_KEY = "book.totem.manual.cover";

    private static final String BOOK_TITLE = "Totem Manual";
    private static final String BOOK_AUTHOR = "Totem";
    private static final String MARKER_KEY = "totem_manual";
    private static final String SCHEMA_KEY = "totem_manual_schema";
    private static final String SECTIONS_KEY = "totem_manual_sections";
    private static final String SUBSET_KEY = "totem_manual_subset";
    private static final String REVISION_KEY = "totem_manual_revision";

    private TotemManualAssembler() {
    }

    public static ItemStack create() {
        ItemStack result = new ItemStack(Items.WRITTEN_BOOK);
        rebuildInternal(result, TotemManualRegistry.global().sections(), false);
        return result;
    }

    public static ItemStack create(List<TotemManualSection> sections) {
        ItemStack result = new ItemStack(Items.WRITTEN_BOOK);
        rebuild(result, sections);
        return result;
    }

    public static void rebuild(ItemStack manual) {
        CompoundTag tag = manual.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        boolean preserveSubset = isCanonical(manual)
                && tag.getIntOr(SCHEMA_KEY, 0) >= SCHEMA_VERSION
                && tag.getBooleanOr(SUBSET_KEY, false);
        List<TotemManualSection> selected = preserveSubset ? sections(manual) : List.of();
        if (preserveSubset && !selected.isEmpty()) {
            rebuildInternal(manual, selected, true);
        } else {
            rebuildInternal(manual, TotemManualRegistry.global().sections(), false);
        }
    }

    public static void rebuild(ItemStack manual, List<TotemManualSection> suppliedSections) {
        List<TotemManualSection> sections = normalized(suppliedSections);
        rebuildInternal(manual, sections, !matchesAllRegistered(sections));
    }

    private static void rebuildInternal(
            ItemStack manual,
            List<TotemManualSection> suppliedSections,
            boolean subset
    ) {
        Objects.requireNonNull(manual, "manual");
        if (!manual.is(Items.WRITTEN_BOOK)) {
            throw new IllegalArgumentException("A Totem manual must use minecraft:written_book");
        }

        List<TotemManualSection> sections = normalized(suppliedSections);
        List<Component> pages = pages(sections);
        validatePageCount(pages.size());

        manual.set(DataComponents.CUSTOM_NAME, sections.size() == 1
                ? Component.translatable(sections.getFirst().titleKey())
                : Component.translatable(MANUAL_NAME_KEY));
        manual.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                Filterable.passThrough(BOOK_TITLE),
                BOOK_AUTHOR,
                0,
                pages.stream().map(Filterable::<Component>passThrough).toList(),
                false
        ));

        CompoundTag tag = manual.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean(MARKER_KEY, true);
        tag.putInt(SCHEMA_KEY, SCHEMA_VERSION);
        tag.putBoolean(SUBSET_KEY, subset);
        tag.putString(SECTIONS_KEY, sections.stream()
                .map(section -> section.id().toString())
                .reduce((left, right) -> left + "," + right)
                .orElse(""));
        tag.putString(REVISION_KEY, revision(sections));
        manual.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static boolean isCanonical(ItemStack stack) {
        if (stack == null || !stack.is(Items.WRITTEN_BOOK)) {
            return false;
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        int schema = tag.getIntOr(SCHEMA_KEY, 0);
        return tag.getBooleanOr(MARKER_KEY, false)
                && schema >= 1
                && schema <= SCHEMA_VERSION;
    }

    public static boolean isCurrent(ItemStack stack) {
        if (!isCanonical(stack)) {
            return false;
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.getIntOr(SCHEMA_KEY, 0) != SCHEMA_VERSION) {
            return false;
        }
        List<TotemManualSection> expected = tag.getBooleanOr(SUBSET_KEY, false)
                ? sections(stack)
                : TotemManualRegistry.global().sections();
        return !expected.isEmpty()
                && sectionIds(expected).equals(tag.getStringOr(SECTIONS_KEY, ""))
                && revision(expected).equals(tag.getStringOr(REVISION_KEY, ""));
    }

    /** Returns the installed chapters represented by this manual. */
    public static List<TotemManualSection> sections(ItemStack stack) {
        if (!isCanonical(stack)) {
            return List.of();
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.getIntOr(SCHEMA_KEY, 0) < SCHEMA_VERSION
                || !tag.getBooleanOr(SUBSET_KEY, false)) {
            return TotemManualRegistry.global().sections();
        }

        Map<Identifier, TotemManualSection> selected = new LinkedHashMap<>();
        for (String rawId : tag.getStringOr(SECTIONS_KEY, "").split(",")) {
            Identifier id = Identifier.tryParse(rawId);
            if (id != null) {
                TotemManualRegistry.global().section(id)
                        .ifPresent(section -> selected.put(section.id(), section));
            }
        }
        return selected.values().stream().sorted().toList();
    }

    public static String revision(List<TotemManualSection> suppliedSections) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (TotemManualSection section : suppliedSections.stream().sorted().toList()) {
                update(digest, section.id().toString());
                update(digest, Integer.toString(section.order()));
                update(digest, section.titleKey());
                for (String pageKey : section.pageKeys()) {
                    update(digest, pageKey);
                    for (Component argument : section.pageArguments()
                            .getOrDefault(pageKey, List.of())) {
                        update(digest, argument.toString());
                    }
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("Java runtime does not provide SHA-256", impossible);
        }
    }

    /** Validates section page usage without creating an ItemStack. */
    public static int validatePageLimit(List<TotemManualSection> suppliedSections) {
        int pageCount = 2 + suppliedSections.stream()
                .mapToInt(section -> 1 + section.pageKeys().size())
                .sum();
        validatePageCount(pageCount);
        return pageCount;
    }

    static List<Component> pages(List<TotemManualSection> sections) {
        List<Component> pages = new ArrayList<>();
        pages.add(Component.translatable(COVER_PAGE_KEY, sections.size()));

        Component contents = Component.translatable("book.totem.manual.contents");
        for (TotemManualSection section : sections) {
            contents = contents.copy()
                    .append(Component.literal("\n• "))
                    .append(Component.translatable(section.titleKey()));
        }
        pages.add(contents);

        for (TotemManualSection section : sections) {
            pages.add(Component.translatable(
                    "book.totem.manual.section",
                    Component.translatable(section.titleKey())
            ));
            section.pageKeys().stream()
                    .map(section::pageComponent)
                    .forEach(pages::add);
        }
        return List.copyOf(pages);
    }

    private static void update(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
    }

    private static List<TotemManualSection> normalized(List<TotemManualSection> suppliedSections) {
        Objects.requireNonNull(suppliedSections, "suppliedSections");
        Map<Identifier, TotemManualSection> unique = new LinkedHashMap<>();
        suppliedSections.stream().sorted().forEach(section -> unique.put(section.id(), section));
        return List.copyOf(unique.values());
    }

    private static boolean matchesAllRegistered(List<TotemManualSection> sections) {
        return sections.stream().map(TotemManualSection::id).toList()
                .equals(TotemManualRegistry.global().sections().stream()
                        .map(TotemManualSection::id).toList());
    }

    private static String sectionIds(List<TotemManualSection> sections) {
        return sections.stream()
                .map(section -> section.id().toString())
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    private static void validatePageCount(int pageCount) {
        if (pageCount > MAX_PAGES) {
            throw new IllegalStateException(
                    "Totem manual requires " + pageCount
                            + " pages; vanilla limit is " + MAX_PAGES
            );
        }
    }
}
