package dev.totem.core.api.v1.manual;

import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Process-local filters for virtual Totem manual pages.
 *
 * <p>Sections keep their complete canonical page-key list in the physical manual metadata, while
 * client-side feature modules may hide individual virtual pages until local synchronized state says
 * they should be visible. Dedicated servers normally have no filters registered, so canonical
 * assembly and persistence remain deterministic.</p>
 */
public final class TotemManualPageFilterRegistry {
    private static final Map<Identifier, Predicate<String>> FILTERS = new LinkedHashMap<>();

    private TotemManualPageFilterRegistry() {
    }

    /**
     * Registers one visibility filter. Filters should return {@code true} for unrelated page keys.
     * A page is visible only when every registered filter accepts it.
     */
    public static synchronized void register(Identifier id, Predicate<String> filter) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(filter, "filter");
        Predicate<String> existing = FILTERS.putIfAbsent(id, filter);
        if (existing != null) {
            throw new IllegalStateException("Duplicate Totem manual page filter: " + id);
        }
    }

    /** Primarily useful for tests or client lifecycle cleanup. */
    public static synchronized void unregister(Identifier id) {
        if (id != null) {
            FILTERS.remove(id);
        }
    }

    public static boolean isVisible(String pageKey) {
        Objects.requireNonNull(pageKey, "pageKey");
        List<Predicate<String>> snapshot;
        synchronized (TotemManualPageFilterRegistry.class) {
            snapshot = List.copyOf(FILTERS.values());
        }
        for (Predicate<String> filter : snapshot) {
            if (!filter.test(pageKey)) {
                return false;
            }
        }
        return true;
    }

    /** Returns the currently visible body-page count for one section. */
    public static int visiblePageCount(TotemManualSection section) {
        Objects.requireNonNull(section, "section");
        return (int) section.pageKeys().stream()
                .filter(TotemManualPageFilterRegistry::isVisible)
                .count();
    }
}
