package dev.totem.core.api.v1.manual;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TotemManualPlayerHelperTest {
    @Test
    void consolidationKeepsTheSortedUnionWithoutDuplicateChapters() {
        TotemManualSection gettingStarted = section("totem:getting_started", -1_000);
        TotemManualSection remnant = section("totem:remnant/manual", 100);
        TotemManualSection villagers = section("totem:villagers/manual", 800);

        List<TotemManualSection> merged = TotemManualPlayerHelper.mergedSections(
                TotemManualPlayerHelper.mergedSections(
                        List.of(villagers, gettingStarted),
                        List.of(remnant, villagers)),
                List.of(villagers));

        assertEquals(
                List.of("totem:getting_started", "totem:remnant/manual", "totem:villagers/manual"),
                merged.stream().map(section -> section.id().toString()).toList());
    }

    private static TotemManualSection section(String id, int order) {
        return new TotemManualSection(
                Identifier.parse(id),
                order,
                "book.test." + id.replace(':', '.'),
                List.of("book.test.page"));
    }
}
