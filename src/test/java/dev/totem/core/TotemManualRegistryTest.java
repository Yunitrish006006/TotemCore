package dev.totem.core;

import dev.totem.core.api.v1.manual.TotemManualAssembler;
import dev.totem.core.api.v1.manual.TotemManualRegistry;
import dev.totem.core.api.v1.manual.TotemManualSection;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TotemManualRegistryTest {
    @Test
    void ordersByDeclaredOrderThenIdentifier() {
        TotemManualRegistry registry = new TotemManualRegistry();
        registry.register(section("totem:z", 10, "page.z"));
        registry.register(section("totem:b", 5, "page.b"));
        registry.register(section("totem:a", 10, "page.a"));

        assertEquals(
                List.of("totem:b", "totem:a", "totem:z"),
                registry.sections().stream().map(value -> value.id().toString()).toList()
        );
    }

    @Test
    void rejectsDuplicateIdentifier() {
        TotemManualRegistry registry = new TotemManualRegistry();
        registry.register(section("totem:duplicate", 0, "page.first"));
        assertThrows(
                IllegalStateException.class,
                () -> registry.register(section("totem:duplicate", 1, "page.second"))
        );
    }

    @Test
    void revisionIsStableAcrossRegistrationOrderAndChangesWithContent() {
        TotemManualSection first = section("totem:first", 10, "page.first");
        TotemManualSection second = section("totem:second", 20, "page.second");
        String ordered = TotemManualAssembler.revision(List.of(first, second));
        String reversed = TotemManualAssembler.revision(List.of(second, first));
        String changed = TotemManualAssembler.revision(List.of(
                first,
                section("totem:second", 20, "page.changed")
        ));

        assertEquals(ordered, reversed);
        assertNotEquals(ordered, changed);
    }

    @Test
    void rejectsContentAboveVanillaPageLimit() {
        TotemManualSection oversized = new TotemManualSection(
                Identifier.parse("totem:oversized"),
                0,
                "book.test.oversized",
                java.util.stream.IntStream.range(0, 98)
                        .mapToObj(page -> "book.test.page." + page)
                        .toList()
        );
        assertThrows(
                IllegalStateException.class,
                () -> TotemManualAssembler.validatePageLimit(List.of(oversized))
        );
    }

    @Test
    void preservesTranslatablePageArguments() {
        String pageKey = "book.test.page.dynamic";
        Component itemName = Component.translatable("item.minecraft.bundle");
        TotemManualSection section = new TotemManualSection(
                Identifier.parse("totem:dynamic"),
                0,
                "book.test.dynamic",
                List.of(pageKey),
                Map.of(pageKey, List.of(itemName))
        );

        assertEquals(
                Component.translatable(pageKey, itemName),
                section.pageComponent(pageKey)
        );
    }

    @Test
    void resolvesRegisteredChapterByIdentifier() {
        TotemManualRegistry registry = new TotemManualRegistry();
        TotemManualSection registered = section("totem:registered", 0, "page.registered");
        registry.register(registered);

        assertEquals(registered, registry.section(registered.id()).orElseThrow());
        assertEquals(true, registry.section(Identifier.parse("totem:missing")).isEmpty());
    }

    private static TotemManualSection section(String id, int order, String page) {
        return new TotemManualSection(
                Identifier.parse(id),
                order,
                "book.test.title." + id.replace(':', '.'),
                List.of(page)
        );
    }
}
