package dev.totem.core.api.v1.manual;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TotemManualChapterRecorderTest {
    @Test
    void appendsNewChaptersWithoutDuplicatingExistingOnes() {
        TotemManualSection base = section("totem:getting_started", -1_000);
        TotemManualSection remnant = section("totem:remnant/manual", 100);
        TotemManualSection nexus = section("totem:nexus/manual", 200);

        List<TotemManualSection> merged = TotemManualChapterRecorder.mergedSections(
                List.of(remnant, base),
                List.of(nexus, remnant)
        );

        assertEquals(
                List.of("totem:getting_started", "totem:remnant/manual", "totem:nexus/manual"),
                merged.stream().map(value -> value.id().toString()).toList()
        );
    }

    private static TotemManualSection section(String id, int order) {
        return new TotemManualSection(
                Identifier.parse(id),
                order,
                "book.test." + id.replace(':', '.'),
                List.of("book.test.page")
        );
    }
}
