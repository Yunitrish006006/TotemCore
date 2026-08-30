package dev.totem.core.api.v1.client.world;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/** Controls whether a world outline participates in normal terrain depth testing. */
@Environment(EnvType.CLIENT)
public enum WorldOutlineOcclusion {
    DEPTH_TESTED,
    THROUGH_WALLS
}
