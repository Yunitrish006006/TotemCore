package dev.totem.core.api.v1.manual;

import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * One localized, independently supplied section of the shared Totem manual.
 *
 * @param id globally unique section identifier
 * @param order primary deterministic sort order
 * @param titleKey translation key used in the contents and section divider
 * @param pageKeys canonical translation keys for the section body pages
 * @param pageArguments optional translatable component arguments by page key
 */
public record TotemManualSection(
        Identifier id,
        int order,
        String titleKey,
        List<String> pageKeys,
        Map<String, List<Component>> pageArguments
) implements Comparable<TotemManualSection> {
    /** Backwards-compatible constructor for pages without component arguments. */
    public TotemManualSection(
            Identifier id,
            int order,
            String titleKey,
            List<String> pageKeys
    ) {
        this(id, order, titleKey, pageKeys, Map.of());
    }

    public TotemManualSection {
        Objects.requireNonNull(id, "id");
        titleKey = requireTranslationKey(titleKey, "titleKey");
        Objects.requireNonNull(pageKeys, "pageKeys");
        if (pageKeys.isEmpty()) {
            throw new IllegalArgumentException("A manual section must contain at least one page: " + id);
        }
        pageKeys = pageKeys.stream()
                .map(key -> requireTranslationKey(key, "pageKey"))
                .toList();
        Objects.requireNonNull(pageArguments, "pageArguments");
        for (String key : pageArguments.keySet()) {
            requireTranslationKey(key, "pageArguments key");
            if (!pageKeys.contains(key)) {
                throw new IllegalArgumentException(
                        "Arguments supplied for a page outside section " + id + ": " + key
                );
            }
        }
        pageArguments = pageArguments.entrySet().stream().collect(
                java.util.stream.Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> List.copyOf(entry.getValue())
                )
        );
    }

    /**
     * Returns the pages currently visible in this process. With no registered filters this is the
     * complete canonical page list; client feature modules can hide discovery-gated pages locally.
     */
    @Override
    public List<String> pageKeys() {
        return pageKeys.stream()
                .filter(TotemManualPageFilterRegistry::isVisible)
                .toList();
    }

    /** Returns the complete registered page list without applying process-local visibility filters. */
    public List<String> canonicalPageKeys() {
        return pageKeys;
    }

    /** Creates a page whose item-name arguments remain translatable at render time. */
    public Component pageComponent(String pageKey) {
        List<Component> arguments = pageArguments.getOrDefault(pageKey, List.of());
        return Component.translatable(pageKey, arguments.toArray());
    }

    @Override
    public int compareTo(TotemManualSection other) {
        int orderComparison = Integer.compare(order, other.order);
        return orderComparison != 0
                ? orderComparison
                : id.toString().compareTo(other.id.toString());
    }

    private static String requireTranslationKey(String value, String label) {
        Objects.requireNonNull(value, label);
        if (value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        return value;
    }
}
