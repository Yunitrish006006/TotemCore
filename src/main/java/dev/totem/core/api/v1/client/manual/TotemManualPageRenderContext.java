package dev.totem.core.api.v1.client.manual;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Client-only rendering context for optional feature-owned manual page overlays. */
@Environment(EnvType.CLIENT)
public record TotemManualPageRenderContext(
        GuiGraphicsExtractor graphics,
        Font font,
        String pageKey,
        int pageLeft,
        int pageTop,
        int mouseX,
        int mouseY
) {
}
