package dev.totem.core.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Map;

public record ClientModuleVersionsPayload(Map<String, String> versions)
        implements CustomPacketPayload {
    public static final Type<ClientModuleVersionsPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("totem-core", "client_module_versions")
    );
    public static final StreamCodec<FriendlyByteBuf, ClientModuleVersionsPayload> CODEC =
            CustomPacketPayload.codec(ClientModuleVersionsPayload::write, ClientModuleVersionsPayload::read);

    public ClientModuleVersionsPayload {
        versions = ModuleVersionSet.immutableOrderedCopy(versions);
    }

    private void write(FriendlyByteBuf buffer) {
        ModuleVersionPayloadCodec.write(buffer, versions);
    }

    private static ClientModuleVersionsPayload read(FriendlyByteBuf buffer) {
        return new ClientModuleVersionsPayload(ModuleVersionPayloadCodec.read(buffer));
    }

    @Override
    public Type<ClientModuleVersionsPayload> type() {
        return TYPE;
    }
}
