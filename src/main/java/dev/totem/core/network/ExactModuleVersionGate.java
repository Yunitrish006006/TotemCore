package dev.totem.core.network;

import dev.totem.core.TotemCore;
import net.fabricmc.fabric.api.networking.v1.FabricServerConfigurationPacketListenerImpl;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ConfigurationTask;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** Rejects clients whose DeadRecall module graph differs from the server. */
public final class ExactModuleVersionGate {
    private static final ConfigurationTask.Type TASK_TYPE =
            new ConfigurationTask.Type("totem-core:exact_module_versions");

    private ExactModuleVersionGate() {
    }

    public static void initializeServer() {
        PayloadTypeRegistry.clientboundConfiguration().register(
                ServerModuleVersionsPayload.TYPE,
                ServerModuleVersionsPayload.CODEC
        );
        PayloadTypeRegistry.serverboundConfiguration().register(
                ClientModuleVersionsPayload.TYPE,
                ClientModuleVersionsPayload.CODEC
        );
        ServerConfigurationNetworking.registerGlobalReceiver(
                ClientModuleVersionsPayload.TYPE,
                ExactModuleVersionGate::receiveClientVersions
        );
        ServerConfigurationConnectionEvents.BEFORE_CONFIGURE.register((listener, server) -> {
            if (!FabricLoader.getInstance().isModLoaded("deadrecall")) {
                return;
            }
            if (!ServerConfigurationNetworking.canSend(listener, ServerModuleVersionsPayload.TYPE)) {
                listener.disconnect(Component.translatableWithFallback(
                        "disconnect.totem_core.version_handshake_required",
                        "DeadRecall requires the same module versions on the client and server."
                ));
                return;
            }
            ((FabricServerConfigurationPacketListenerImpl) listener)
                    .addTask(new ExactModuleVersionTask(ModuleVersionSet.loaded()));
        });
    }

    private static void receiveClientVersions(
            ClientModuleVersionsPayload payload,
            ServerConfigurationNetworking.Context context
    ) {
        if (!FabricLoader.getInstance().isModLoaded("deadrecall")) {
            return;
        }

        List<ModuleVersionSet.Mismatch> mismatches =
                ModuleVersionSet.compare(ModuleVersionSet.loaded(), payload.versions());
        if (!mismatches.isEmpty()) {
            String details = mismatches.stream()
                    .map(mismatch -> mismatch.modId()
                            + " (Server: " + mismatch.serverVersion()
                            + ", Client: " + mismatch.clientVersion() + ")")
                    .collect(Collectors.joining("\n"));
            TotemCore.LOGGER.warn(
                    "Rejected client due to exact module version mismatch: {}",
                    details.replace('\n', ';')
            );
            context.packetListener().disconnect(Component.translatableWithFallback(
                    "disconnect.totem_core.module_version_mismatch",
                    "TOTEM module versions do not match the server:\n%s",
                    details
            ));
            return;
        }

        ((FabricServerConfigurationPacketListenerImpl) context.packetListener())
                .completeTask(TASK_TYPE);
    }

    private record ExactModuleVersionTask(Map<String, String> serverVersions)
            implements ConfigurationTask {
        private ExactModuleVersionTask {
            serverVersions = ModuleVersionSet.immutableOrderedCopy(serverVersions);
        }

        @Override
        public void start(Consumer<Packet<?>> sender) {
            sender.accept(ServerConfigurationNetworking.createClientboundPacket(
                    new ServerModuleVersionsPayload(serverVersions)
            ));
        }

        @Override
        public Type type() {
            return TASK_TYPE;
        }
    }
}
