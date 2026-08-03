package dev.totem.core.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Map;

public record ServerModuleVersionsPayload(Map<String, String> versions)
        implements CustomPacketPayload {
    public static final Type<ServerModuleVersionsPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("totem-core", "server_module_versions")
    );
    public static final StreamCodec<FriendlyByteBuf, ServerModuleVersionsPayload> CODEC =
            CustomPacketPayload.codec(ServerModuleVersionsPayload::write, ServerModuleVersionsPayload::read);

    public ServerModuleVersionsPayload {
        versions = ModuleVersionSet.immutableOrderedCopy(versions);
    }

    private void write(FriendlyByteBuf buffer) {
        ModuleVersionPayloadCodec.write(buffer, versions);
    }

    private static ServerModuleVersionsPayload read(FriendlyByteBuf buffer) {
        return new ServerModuleVersionsPayload(ModuleVersionPayloadCodec.read(buffer));
    }

    @Override
    public Type<ServerModuleVersionsPayload> type() {
        return TYPE;
    }
}
