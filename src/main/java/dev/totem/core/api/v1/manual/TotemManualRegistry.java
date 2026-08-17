package dev.totem.core.api.v1.manual;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Duplicate-rejecting process-local registry for installed manual sections. */
public final class TotemManualRegistry {
    private static final TotemManualRegistry GLOBAL = new TotemManualRegistry();

    private final Map<Identifier, TotemManualSection> sections = new LinkedHashMap<>();

    public static TotemManualRegistry global() {
        return GLOBAL;
    }

    public synchronized void register(TotemManualSection section) {
        Objects.requireNonNull(section, "section");
        TotemManualSection existing = sections.putIfAbsent(section.id(), section);
        if (existing != null) {
            throw new IllegalStateException(
                    "Totem manual section ID is already registered: " + section.id()
            );
        }
    }

    public synchronized List<TotemManualSection> sections() {
        List<TotemManualSection> snapshot = new ArrayList<>(sections.values());
        Collections.sort(snapshot);
        return List.copyOf(snapshot);
    }

    public synchronized Optional<TotemManualSection> section(Identifier id) {
        return Optional.ofNullable(sections.get(Objects.requireNonNull(id, "id")));
    }
}
