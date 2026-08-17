package dev.totem.core.api.v1.client.manual;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/** Draws optional feature-specific content after Core renders a canonical manual page. */
@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface TotemManualPageOverlay {
    void render(TotemManualPageRenderContext context);
}
