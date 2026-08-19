package dev.totem.core.social;

import dev.totem.core.api.v1.social.FriendActionResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotemFriendSavedDataTest {
    private static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID CAROL = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Test
    void preservesLegacyStorageIdentifier() {
        assertEquals("deadrecall:space_friends", TotemFriendSavedData.STORAGE_ID.toString());
    }

    @Test
    void reciprocalInviteCreatesMutualFriendship() {
        TotemFriendSavedData data = new TotemFriendSavedData();

        assertEquals(FriendActionResult.INVITED, data.inviteOrAccept(ALICE, BOB));
        assertEquals(List.of(BOB), data.outgoingInviteTargets(ALICE));
        assertEquals(List.of(ALICE), data.incomingInviteSources(BOB));

        assertEquals(FriendActionResult.ACCEPTED, data.inviteOrAccept(BOB, ALICE));
        assertTrue(data.areFriends(ALICE, BOB));
        assertTrue(data.areFriends(BOB, ALICE));
        assertEquals(List.of(BOB), data.friendsOf(ALICE));
        assertTrue(data.outgoingInviteTargets(ALICE).isEmpty());
        assertTrue(data.incomingInviteSources(BOB).isEmpty());
    }

    @Test
    void relationshipsRemainCanonicalAndDeterministic() {
        TotemFriendSavedData data = new TotemFriendSavedData();
        data.inviteOrAccept(CAROL, ALICE);
        data.inviteOrAccept(ALICE, CAROL);
        data.inviteOrAccept(BOB, ALICE);
        data.inviteOrAccept(ALICE, BOB);

        assertEquals(List.of(BOB, CAROL), data.friendsOf(ALICE));
        assertTrue(data.removeRelationship(CAROL, ALICE));
        assertFalse(data.areFriends(ALICE, CAROL));
        assertTrue(data.areFriends(ALICE, BOB));
    }

    @Test
    void invalidAndDuplicateActionsAreSafe() {
        TotemFriendSavedData data = new TotemFriendSavedData();
        assertEquals(FriendActionResult.INVALID, data.inviteOrAccept(ALICE, ALICE));
        assertEquals(FriendActionResult.INVITED, data.inviteOrAccept(ALICE, BOB));
        assertEquals(FriendActionResult.PENDING, data.inviteOrAccept(ALICE, BOB));
        assertEquals(FriendActionResult.ACCEPTED, data.inviteOrAccept(BOB, ALICE));
        assertEquals(FriendActionResult.ALREADY_FRIENDS, data.inviteOrAccept(ALICE, BOB));
    }
}
