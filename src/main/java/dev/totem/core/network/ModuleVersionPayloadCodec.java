package dev.totem.core.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

import java.util.LinkedHashMap;
import java.util.Map;

final class ModuleVersionPayloadCodec {
    private static final int MAX_MOD_ID_LENGTH = 128;
    private static final int MAX_VERSION_LENGTH = 128;

    private ModuleVersionPayloadCodec() {
    }

    static void write(FriendlyByteBuf buffer, Map<String, String> versions) {
        buffer.writeVarInt(ModuleVersionSet.MODULE_IDS.size());
        for (String modId : ModuleVersionSet.MODULE_IDS) {
            buffer.writeUtf(modId, MAX_MOD_ID_LENGTH);
            buffer.writeUtf(versions.getOrDefault(modId, ModuleVersionSet.MISSING), MAX_VERSION_LENGTH);
        }
    }

    static Map<String, String> read(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count != ModuleVersionSet.MODULE_IDS.size()) {
            throw new DecoderException("Expected " + ModuleVersionSet.MODULE_IDS.size()
                    + " module versions, received " + count);
        }

        LinkedHashMap<String, String> versions = new LinkedHashMap<>();
        for (int index = 0; index < count; index++) {
            String modId = buffer.readUtf(MAX_MOD_ID_LENGTH);
            String version = buffer.readUtf(MAX_VERSION_LENGTH);
            if (!ModuleVersionSet.MODULE_IDS.contains(modId) || versions.put(modId, version) != null) {
                throw new DecoderException("Unexpected or duplicate module id: " + modId);
            }
        }
        if (!versions.keySet().containsAll(ModuleVersionSet.MODULE_IDS)) {
            throw new DecoderException("Module version list is incomplete");
        }
        return versions;
    }
}
