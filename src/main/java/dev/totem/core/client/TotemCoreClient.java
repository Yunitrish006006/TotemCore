package dev.totem.core.client;

import dev.totem.core.network.ClientModuleVersionsPayload;
import dev.totem.core.network.ModuleVersionSet;
import dev.totem.core.network.ServerModuleVersionsPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;

/** Registers the client half of DeadRecall's pre-join exact-version handshake. */
public final class TotemCoreClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientConfigurationNetworking.registerGlobalReceiver(
                ServerModuleVersionsPayload.TYPE,
                (payload, context) -> context.responseSender().sendPacket(
                        new ClientModuleVersionsPayload(ModuleVersionSet.loaded())
                )
        );
    }
}
