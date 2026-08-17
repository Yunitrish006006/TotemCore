package dev.totem.core.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client request to split the canonical Totem manual currently held by the player. */
public record SplitTotemManualPayload() implements CustomPacketPayload {
    public static final SplitTotemManualPayload INSTANCE = new SplitTotemManualPayload();
    public static final Type<SplitTotemManualPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("totem-core", "split_manual")
    );
    public static final StreamCodec<FriendlyByteBuf, SplitTotemManualPayload> CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
