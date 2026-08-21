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

    @Test
    void moduleGuidePlanKeepsCanonicalReferencesSeparateAndAvoidsDuplicates() {
        assertEquals(
                TotemManualPlayerHelper.ModuleGuideAction.CREATE_FROM_REFERENCE,
                TotemManualPlayerHelper.moduleGuideAction(
                        false, false, false, false, false, false, true),
                "a basic or other canonical guide must be retained as a recording reference"
        );
        assertEquals(
                TotemManualPlayerHelper.ModuleGuideAction.REFRESH_ACTIVE,
                TotemManualPlayerHelper.moduleGuideAction(
                        false, true, false, false, true, false, true),
                "an active exact target guide must be refreshed instead of duplicated"
        );
        assertEquals(
                TotemManualPlayerHelper.ModuleGuideAction.REFRESH_EXISTING,
                TotemManualPlayerHelper.moduleGuideAction(
                        false, false, false, false, true, true, false),
                "a carried target guide must be refreshed without consuming the active plain book"
        );
        assertEquals(
                TotemManualPlayerHelper.ModuleGuideAction.CONSOLIDATE_ACTIVE,
                TotemManualPlayerHelper.moduleGuideAction(
                        false, true, false, true, true, false, true),
                "duplicate exact target guides in both hands must still consolidate"
        );
    }

    @Test
    void manualDeliveryDropsWhenNoInventorySlotCanAcceptTheGuide() {
        assertEquals(
                TotemManualPlayerHelper.ManualDeliveryAction.DROP,
                TotemManualPlayerHelper.manualDeliveryAction(-1, -1),
                "a completely full inventory must drop the guide instead of trusting creative insertion"
        );
        assertEquals(
                TotemManualPlayerHelper.ManualDeliveryAction.INSERT,
                TotemManualPlayerHelper.manualDeliveryAction(4, -1),
                "a free slot should accept the guide"
        );
        assertEquals(
                TotemManualPlayerHelper.ManualDeliveryAction.INSERT,
                TotemManualPlayerHelper.manualDeliveryAction(-1, 7),
                "a compatible partial stack should accept the guide"
        );
    }

    private static TotemManualSection section(String id, int order) {
        return new TotemManualSection(
                Identifier.parse(id),
                order,
                "book.test." + id.replace(':', '.'),
                List.of("book.test.page"));
    }
}
