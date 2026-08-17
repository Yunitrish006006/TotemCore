package dev.totem.core.network;

import dev.totem.core.api.v1.manual.TotemManualPlayerHelper;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/** Registers the server-authoritative manual UI actions shared by every Totem module. */
public final class TotemManualPayloadRegistration {
    private TotemManualPayloadRegistration() {
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(
                SplitTotemManualPayload.TYPE,
                SplitTotemManualPayload.CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(
                SplitTotemManualPayload.TYPE,
                (payload, context) -> context.server().execute(() ->
                        TotemManualPlayerHelper.splitHeldManual(context.player()))
        );
    }
}
