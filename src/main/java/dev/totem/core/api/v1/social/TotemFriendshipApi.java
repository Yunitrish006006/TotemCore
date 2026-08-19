package dev.totem.core.api.v1.social;

import dev.totem.core.social.TotemFriendSavedData;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Stable server-authoritative friendship contract shared by Totem modules.
 *
 * <p>Friendships are mutual. Invitations are directional until accepted by a
 * reciprocal invitation. Feature modules should use this API rather than own
 * or mirror social relationship state.</p>
 */
public final class TotemFriendshipApi {
    private TotemFriendshipApi() { }

    public static boolean areFriends(MinecraftServer server, UUID first, UUID second) {
        return data(server).areFriends(first, second);
    }

    /** Compatibility-friendly synonym for integrations that describe friendships as mutual. */
    public static boolean areMutualFriends(MinecraftServer server, UUID first, UUID second) {
        return areFriends(server, first, second);
    }

    public static List<UUID> friendsOf(MinecraftServer server, UUID playerId) {
        return data(server).friendsOf(playerId);
    }

    public static List<UUID> outgoingInvites(MinecraftServer server, UUID playerId) {
        return data(server).outgoingInviteTargets(playerId);
    }

    public static List<UUID> incomingInvites(MinecraftServer server, UUID playerId) {
        return data(server).incomingInviteSources(playerId);
    }

    public static FriendActionResult inviteOrAccept(MinecraftServer server, UUID from, UUID to) {
        return data(server).inviteOrAccept(from, to);
    }

    /** Removes either an existing friendship or any pending invitation between the pair. */
    public static boolean removeRelationship(MinecraftServer server, UUID first, UUID second) {
        return data(server).removeRelationship(first, second);
    }

    private static TotemFriendSavedData data(MinecraftServer server) {
        Objects.requireNonNull(server, "server");
        return server.overworld().getDataStorage().computeIfAbsent(TotemFriendSavedData.TYPE);
    }
}
