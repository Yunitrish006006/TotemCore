package dev.totem.core.client;

import dev.totem.core.api.v1.client.manual.TotemManualPageOverlayRegistry;
import dev.totem.core.api.v1.client.manual.TotemManualPageRenderContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Renders concrete in-game source blocks instead of spelling every source out as prose. */
final class TotemStarterManualOverlay {
    private static final String PAGE_2 = "book.totem.manual.getting_started.page.2";
    private static final String PAGE_3 = "book.totem.manual.getting_started.page.3";
    private static final int INK = 0xFF4B3826;
    private static final int MUTED = 0xFF765B3D;
    private static final int LABEL_X = 36;
    private static final int ICON_RIGHT_X = 142;
    private static final int ICON_SPACING = 20;

    private TotemStarterManualOverlay() {
    }

    static void register() {
        TotemManualPageOverlayRegistry.register(
                Identifier.fromNamespaceAndPath("totem-core", "starter_source_items"),
                TotemStarterManualOverlay::render
        );
    }

    private static void render(TotemManualPageRenderContext context) {
        if (PAGE_2.equals(context.pageKey())) {
            renderFirstSourcePage(context);
        } else if (PAGE_3.equals(context.pageKey())) {
            renderSecondSourcePage(context);
        }
    }

    private static void renderFirstSourcePage(TotemManualPageRenderContext context) {
        int y = context.pageTop() + 76;
        row(context, "Alchemy", y,
                Items.BREWING_STAND,
                Items.CAULDRON,
                item("totem:alchemy_cauldron"));
        row(context, "Remnant", y + 22, Items.SMITHING_TABLE);
        row(context, "Automata", y + 44, item("minecraft:copper_chest"));
        row(context, "Enchanting", y + 66, Items.ENCHANTING_TABLE);
    }

    private static void renderSecondSourcePage(TotemManualPageRenderContext context) {
        int y = context.pageTop() + 66;
        row(context, "Nexus", y, Items.LODESTONE);
        row(context, "Excavation", y + 19, Items.CRAFTING_TABLE);
        row(context, "Locksmith", y + 38,
                Items.CHEST,
                Items.TRAPPED_CHEST,
                Items.BARREL);
        row(context, "Vanilla Tweaks", y + 57, Items.LECTERN);
        row(context, "Villagers", y + 76, Items.COMPOSTER);
    }

    private static void row(TotemManualPageRenderContext context, String label, int y, Item... items) {
        int labelX = context.pageLeft() + LABEL_X;
        context.graphics().text(context.font(), label, labelX, y + 4, INK, false);

        int valid = 0;
        for (Item item : items) {
            if (item != null && item != Items.AIR) {
                valid++;
            }
        }
        if (valid == 0) {
            context.graphics().text(context.font(), "?", context.pageLeft() + ICON_RIGHT_X, y + 4, MUTED, false);
            return;
        }

        int startX = context.pageLeft() + ICON_RIGHT_X - (valid - 1) * ICON_SPACING;
        int index = 0;
        for (Item item : items) {
            if (item == null || item == Items.AIR) {
                continue;
            }
            int x = startX + index * ICON_SPACING;
            stack(context, new ItemStack(item), x, y);
            index++;
        }
    }

    private static void stack(TotemManualPageRenderContext context, ItemStack stack, int x, int y) {
        context.graphics().item(stack, x, y);
        if (inside(context, x, y, 16, 16)) {
            context.graphics().setTooltipForNextFrame(
                    context.font(), stack, context.mouseX(), context.mouseY());
        }
    }

    private static Item item(String id) {
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
        return item == null ? Items.AIR : item;
    }

    private static boolean inside(TotemManualPageRenderContext context,
                                  int x, int y, int width, int height) {
        return context.mouseX() >= x && context.mouseX() < x + width
                && context.mouseY() >= y && context.mouseY() < y + height;
    }
}
