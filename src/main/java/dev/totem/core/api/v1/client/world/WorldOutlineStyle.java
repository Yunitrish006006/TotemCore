package dev.totem.core.api.v1.client.world;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Objects;

/** Immutable client-only style for a submitted world outline. */
@Environment(EnvType.CLIENT)
public record WorldOutlineStyle(int argb, float lineWidth, WorldOutlineOcclusion occlusion) {
    public WorldOutlineStyle {
        if (!Float.isFinite(lineWidth) || lineWidth <= 0.0F) {
            throw new IllegalArgumentException("World outline line width must be finite and positive");
        }
        Objects.requireNonNull(occlusion, "occlusion");
    }
}
