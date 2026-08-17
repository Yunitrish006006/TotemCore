package dev.totem.core.api.v1.client.manual;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Process-local client registry for feature-specific canonical manual page overlays. */
@Environment(EnvType.CLIENT)
public final class TotemManualPageOverlayRegistry {
    private static final Map<Identifier, TotemManualPageOverlay> OVERLAYS = new LinkedHashMap<>();

    private TotemManualPageOverlayRegistry() {
    }

    public static synchronized void register(Identifier id, TotemManualPageOverlay overlay) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(overlay, "overlay");
        TotemManualPageOverlay existing = OVERLAYS.putIfAbsent(id, overlay);
        if (existing != null) {
            throw new IllegalStateException("Duplicate Totem manual page overlay: " + id);
        }
    }

    public static void render(TotemManualPageRenderContext context) {
        List<TotemManualPageOverlay> snapshot;
        synchronized (TotemManualPageOverlayRegistry.class) {
            snapshot = List.copyOf(OVERLAYS.values());
        }
        snapshot.forEach(overlay -> overlay.render(context));
    }
}
