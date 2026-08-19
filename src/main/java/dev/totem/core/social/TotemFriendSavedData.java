package dev.totem.core.social;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.totem.core.api.v1.social.FriendActionResult;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Canonical Totem-wide friendship storage.
 *
 * <p>The persisted identifier intentionally remains {@code deadrecall:space_friends}
 * so worlds created by pre-Core-0.7 Nexus builds load the exact same friendships
 * and pending invitations without a destructive copy migration.</p>
 */
public final class TotemFriendSavedData extends SavedData {
    public static final int DATA_VERSION = 1;
    public static final Identifier STORAGE_ID = Identifier.fromNamespaceAndPath("deadrecall", "space_friends");

    private static final Codec<Friendship> FRIENDSHIP_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("first").forGetter(Friendship::first),
            UUIDUtil.CODEC.fieldOf("second").forGetter(Friendship::second)
    ).apply(instance, Friendship::new));

    private static final Codec<PendingFriendInvite> PENDING_INVITE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("from").forGetter(PendingFriendInvite::from),
            UUIDUtil.CODEC.fieldOf("to").forGetter(PendingFriendInvite::to)
    ).apply(instance, PendingFriendInvite::new));

    public static final Codec<TotemFriendSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("data_version", DATA_VERSION).forGetter(TotemFriendSavedData::dataVersion),
            FRIENDSHIP_CODEC.listOf().optionalFieldOf("friendships", List.of()).forGetter(TotemFriendSavedData::friendshipList),
            PENDING_INVITE_CODEC.listOf().optionalFieldOf("pending_invites", List.of()).forGetter(TotemFriendSavedData::pendingInviteList)
    ).apply(instance, TotemFriendSavedData::new));

    public static final SavedDataType<TotemFriendSavedData> TYPE = new SavedDataType<>(
            STORAGE_ID,
            TotemFriendSavedData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final int dataVersion;
    private final Set<Friendship> friendships = new HashSet<>();
    private final Set<PendingFriendInvite> pendingInvites = new HashSet<>();

    public TotemFriendSavedData() {
        this(DATA_VERSION, List.of(), List.of());
    }

    private TotemFriendSavedData(int dataVersion, List<Friendship> friendships, List<PendingFriendInvite> pendingInvites) {
        this.dataVersion = Math.max(dataVersion, DATA_VERSION);
        this.friendships.addAll(friendships);
        this.pendingInvites.addAll(pendingInvites);
    }

    public boolean areFriends(UUID first, UUID second) {
        if (first == null || second == null || first.equals(second)) {
            return false;
        }
        return friendships.contains(new Friendship(first, second));
    }

    public List<UUID> friendsOf(UUID playerId) {
        if (playerId == null) return List.of();
        List<UUID> result = new ArrayList<>();
        for (Friendship friendship : friendships) {
            if (friendship.first().equals(playerId)) {
                result.add(friendship.second());
            } else if (friendship.second().equals(playerId)) {
                result.add(friendship.first());
            }
        }
        result.sort(Comparator.naturalOrder());
        return List.copyOf(result);
    }

    public List<UUID> outgoingInviteTargets(UUID playerId) {
        if (playerId == null) return List.of();
        return pendingInvites.stream()
                .filter(invite -> invite.from().equals(playerId))
                .map(PendingFriendInvite::to)
                .sorted()
                .toList();
    }

    public List<UUID> incomingInviteSources(UUID playerId) {
        if (playerId == null) return List.of();
        return pendingInvites.stream()
                .filter(invite -> invite.to().equals(playerId))
                .map(PendingFriendInvite::from)
                .sorted()
                .toList();
    }

    public FriendActionResult inviteOrAccept(UUID from, UUID to) {
        if (from == null || to == null || from.equals(to)) {
            return FriendActionResult.INVALID;
        }
        if (areFriends(from, to)) {
            return FriendActionResult.ALREADY_FRIENDS;
        }

        PendingFriendInvite reverse = new PendingFriendInvite(to, from);
        if (pendingInvites.remove(reverse)) {
            pendingInvites.remove(new PendingFriendInvite(from, to));
            friendships.add(new Friendship(from, to));
            setDirty();
            return FriendActionResult.ACCEPTED;
        }

        PendingFriendInvite direct = new PendingFriendInvite(from, to);
        if (pendingInvites.add(direct)) {
            setDirty();
            return FriendActionResult.INVITED;
        }
        return FriendActionResult.PENDING;
    }

    public boolean removeRelationship(UUID first, UUID second) {
        if (first == null || second == null || first.equals(second)) {
            return false;
        }
        boolean removed = friendships.remove(new Friendship(first, second));
        removed |= pendingInvites.remove(new PendingFriendInvite(first, second));
        removed |= pendingInvites.remove(new PendingFriendInvite(second, first));
        if (removed) setDirty();
        return removed;
    }

    private int dataVersion() {
        return dataVersion;
    }

    private List<Friendship> friendshipList() {
        return friendships.stream()
                .sorted(Comparator.comparing(Friendship::first).thenComparing(Friendship::second))
                .toList();
    }

    private List<PendingFriendInvite> pendingInviteList() {
        return pendingInvites.stream()
                .sorted(Comparator.comparing(PendingFriendInvite::from).thenComparing(PendingFriendInvite::to))
                .toList();
    }

    private record Friendship(UUID first, UUID second) {
        private Friendship {
            if (first.compareTo(second) > 0) {
                UUID swap = first;
                first = second;
                second = swap;
            }
        }
    }

    private record PendingFriendInvite(UUID from, UUID to) { }
}
