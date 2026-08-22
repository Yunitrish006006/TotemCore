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
 * Canonical Totem manual assembler.
 *
 * <p>The physical WrittenBookContent deliberately stays tiny: it contains only a cover and a
 * section-index marker. The client expands that index into an unlimited virtual page sequence, so
 * gameplay content is no longer constrained by vanilla's written-book page cap.</p>
 */
public final class TotemManualAssembler {
    public static final int SCHEMA_VERSION = 3;
    /** Kept for source compatibility. Virtual Totem manuals are no longer page-limited. */
    @Deprecated(forRemoval = false)
    public static final int MAX_PAGES = Integer.MAX_VALUE;
    public static final int CONTENTS_ENTRIES_PER_PAGE = 10;
    public static final String MANUAL_NAME_KEY = "item.totem.manual";
    public static final String COVER_PAGE_KEY = "book.totem.manual.cover";
    public static final String CONTENTS_PAGE_KEY = "book.totem.manual.contents";
    public static final String MANUAL_INDEX_PREFIX = "totem_manual_index:";

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
        List<TotemManualSection> selected = isCanonical(manual)
                ? sections(manual)
                : TotemManualRegistry.global().sections();
        if (selected.isEmpty()) {
            selected = TotemManualRegistry.global().sections();
        }
        rebuildInternal(manual, selected, !matchesAllRegistered(selected));
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
        String ids = sectionIds(sections);
        List<Component> physicalPages = List.of(
                Component.translatable(COVER_PAGE_KEY, sections.size()),
                Component.literal(MANUAL_INDEX_PREFIX + ids)
        );

        manual.set(DataComponents.CUSTOM_NAME, Component.translatable(MANUAL_NAME_KEY));
        manual.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                Filterable.passThrough(BOOK_TITLE),
                BOOK_AUTHOR,
                0,
                physicalPages.stream().map(Filterable::<Component>passThrough).toList(),
                false
        ));

        CompoundTag tag = manual.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean(MARKER_KEY, true);
        tag.putInt(SCHEMA_KEY, SCHEMA_VERSION);
        // Retain the old field so schema-2 clients fail predictably instead of silently losing metadata.
        tag.putBoolean(SUBSET_KEY, subset);
        tag.putString(SECTIONS_KEY, ids);
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
        List<TotemManualSection> expected = sections(stack);
        return !expected.isEmpty()
                && sectionIds(expected).equals(tag.getStringOr(SECTIONS_KEY, ""))
                && revision(expected).equals(tag.getStringOr(REVISION_KEY, ""));
    }

    /** Returns the exact recorded chapters represented by this manual. */
    public static List<TotemManualSection> sections(ItemStack stack) {
        if (!isCanonical(stack)) {
            return List.of();
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        int schema = tag.getIntOr(SCHEMA_KEY, 0);
        // Schema 1 and full schema-2 manuals represented the then-current global manual.
        if (schema < 2 || schema == 2 && !tag.getBooleanOr(SUBSET_KEY, false)) {
            return TotemManualRegistry.global().sections();
        }
        return resolveSectionIds(tag.getStringOr(SECTIONS_KEY, ""));
    }

    /** Resolves the compact physical index page into locally registered chapter definitions. */
    public static List<TotemManualSection> sectionsFromIndexPage(Component indexPage) {
        if (indexPage == null) {
            return List.of();
        }
        String raw = indexPage.getString();
        if (!raw.startsWith(MANUAL_INDEX_PREFIX)) {
            return List.of();
        }
        return resolveSectionIds(raw.substring(MANUAL_INDEX_PREFIX.length()));
    }

    /** Builds the client-visible page sequence. This list has no vanilla written-book page limit. */
    public static List<Component> virtualPages(List<TotemManualSection> suppliedSections) {
        List<TotemManualSection> sections = normalized(suppliedSections);
        List<Component> pages = new ArrayList<>();
        pages.add(Component.translatable(COVER_PAGE_KEY, sections.size()));
        for (int page = 0; page < contentsPageCount(sections.size()); page++) {
            pages.add(Component.translatable(CONTENTS_PAGE_KEY));
        }
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

    public static int contentsPageCount(int sectionCount) {
        if (sectionCount < 0) {
            throw new IllegalArgumentException("sectionCount must not be negative");
        }
        return Math.max(1, (sectionCount + CONTENTS_ENTRIES_PER_PAGE - 1)
                / CONTENTS_ENTRIES_PER_PAGE);
    }

    /** Returns the zero-based virtual page containing the requested chapter divider. */
    public static int sectionStartPage(
            List<TotemManualSection> suppliedSections,
            int sectionIndex
    ) {
        List<TotemManualSection> sections = normalized(suppliedSections);
        if (sectionIndex < 0 || sectionIndex >= sections.size()) {
            throw new IndexOutOfBoundsException("Manual section index: " + sectionIndex);
        }
        int page = 1 + contentsPageCount(sections.size());
        for (int index = 0; index < sectionIndex; index++) {
            page += 1 + sections.get(index).pageKeys().size();
        }
        return page;
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

    /**
     * Compatibility helper retained under its old name. It now reports logical virtual pages and does
     * not enforce vanilla's physical WrittenBookContent limit.
     */
    public static int validatePageLimit(List<TotemManualSection> suppliedSections) {
        return virtualPages(suppliedSections).size();
    }

    /** Backwards-compatible package-local alias used by older tests. */
    static List<Component> pages(List<TotemManualSection> sections) {
        return virtualPages(sections);
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

    private static List<TotemManualSection> resolveSectionIds(String ids) {
        Map<Identifier, TotemManualSection> selected = new LinkedHashMap<>();
        for (String rawId : ids.split(",")) {
            Identifier id = Identifier.tryParse(rawId);
            if (id != null) {
                TotemManualRegistry.global().section(id)
                        .ifPresent(section -> selected.put(section.id(), section));
            }
        }
        return selected.values().stream().sorted().toList();
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
}
