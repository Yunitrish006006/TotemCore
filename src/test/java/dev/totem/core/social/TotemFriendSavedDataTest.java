package dev.totem.core.social;

import dev.totem.core.api.v1.social.FriendActionResult;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotemFriendSavedDataTest {
    private static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID CAROL = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @BeforeAll
    static void initializeSavedDataVersion() {
        SharedConstants.tryDetectVersion();
    }

    @Test
    void usesCanonicalStorageIdentifier() {
        assertEquals("totem:space_friends", TotemFriendSavedData.STORAGE_ID.toString());
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

    @Test
    void emptyStorageCreatesOnlyTheCanonicalFriendshipData(@TempDir Path temporaryDirectory) {
        try (SavedDataStorage storage = storage(temporaryDirectory)) {
            TotemFriendSavedData data = TotemFriendSavedData.loadCanonical(storage);

            assertSame(data, storage.get(TotemFriendSavedData.TYPE));
            assertNull(storage.get(TotemFriendSavedData.LEGACY_COMPATIBILITY_TYPE));
            assertEquals(FriendActionResult.INVITED, data.inviteOrAccept(ALICE, BOB));
            storage.saveAndJoin();
        }

        try (SavedDataStorage reloadedStorage = storage(temporaryDirectory)) {
            TotemFriendSavedData reloaded = TotemFriendSavedData.loadCanonical(reloadedStorage);

            assertEquals(List.of(BOB), reloaded.outgoingInviteTargets(ALICE));
            assertNull(reloadedStorage.get(TotemFriendSavedData.LEGACY_COMPATIBILITY_TYPE));
        }
    }

    @Test
    void migratesLegacyDataToCanonicalStorageWithoutWritingBack(@TempDir Path temporaryDirectory) {
        try (SavedDataStorage legacyStorage = storage(temporaryDirectory)) {
            TotemFriendSavedData legacy = new TotemFriendSavedData();
            legacy.inviteOrAccept(ALICE, BOB);
            legacy.inviteOrAccept(BOB, ALICE);
            legacyStorage.set(TotemFriendSavedData.LEGACY_COMPATIBILITY_TYPE, legacy);
            legacyStorage.saveAndJoin();
        }

        try (SavedDataStorage migratingStorage = storage(temporaryDirectory)) {
            TotemFriendSavedData migrated = TotemFriendSavedData.loadCanonical(migratingStorage);

            assertTrue(migrated.areFriends(ALICE, BOB));
            assertEquals(FriendActionResult.INVITED, migrated.inviteOrAccept(ALICE, CAROL));
            migratingStorage.saveAndJoin();
        }

        try (SavedDataStorage reloadedStorage = storage(temporaryDirectory)) {
            TotemFriendSavedData canonical = reloadedStorage.get(TotemFriendSavedData.TYPE);
            TotemFriendSavedData legacy = reloadedStorage.get(TotemFriendSavedData.LEGACY_COMPATIBILITY_TYPE);

            assertTrue(canonical.areFriends(ALICE, BOB));
            assertEquals(List.of(CAROL), canonical.outgoingInviteTargets(ALICE));
            assertTrue(legacy.areFriends(ALICE, BOB));
            assertTrue(legacy.outgoingInviteTargets(ALICE).isEmpty());
        }
    }

    @Test
    void migrationPersistsCanonicalDataWithoutAnyLaterMutation(@TempDir Path temporaryDirectory) {
        try (SavedDataStorage legacyStorage = storage(temporaryDirectory)) {
            TotemFriendSavedData legacy = new TotemFriendSavedData();
            legacy.inviteOrAccept(ALICE, BOB);
            legacy.inviteOrAccept(BOB, ALICE);
            legacyStorage.set(TotemFriendSavedData.LEGACY_COMPATIBILITY_TYPE, legacy);
            legacyStorage.saveAndJoin();
        }

        try (SavedDataStorage migratingStorage = storage(temporaryDirectory)) {
            TotemFriendSavedData migrated = TotemFriendSavedData.loadCanonical(migratingStorage);

            assertTrue(migrated.areFriends(ALICE, BOB));
            migratingStorage.saveAndJoin();
        }

        try (SavedDataStorage reloadedStorage = storage(temporaryDirectory)) {
            TotemFriendSavedData canonical = reloadedStorage.get(TotemFriendSavedData.TYPE);

            assertTrue(canonical.areFriends(ALICE, BOB));
        }
    }

    @Test
    void existingCanonicalDataWinsOverAConflictingLegacyCopy(@TempDir Path temporaryDirectory) {
        try (SavedDataStorage storage = storage(temporaryDirectory)) {
            TotemFriendSavedData legacy = new TotemFriendSavedData();
            legacy.inviteOrAccept(ALICE, BOB);
            legacy.inviteOrAccept(BOB, ALICE);
            storage.set(TotemFriendSavedData.LEGACY_COMPATIBILITY_TYPE, legacy);

            TotemFriendSavedData canonical = new TotemFriendSavedData();
            canonical.inviteOrAccept(ALICE, CAROL);
            canonical.inviteOrAccept(CAROL, ALICE);
            storage.set(TotemFriendSavedData.TYPE, canonical);
            storage.saveAndJoin();
        }

        try (SavedDataStorage reloadedStorage = storage(temporaryDirectory)) {
            TotemFriendSavedData selected = TotemFriendSavedData.loadCanonical(reloadedStorage);

            assertTrue(selected.areFriends(ALICE, CAROL));
            assertFalse(selected.areFriends(ALICE, BOB));
        }
    }

    private static SavedDataStorage storage(Path directory) {
        return new SavedDataStorage(
                directory,
                DataFixers.getDataFixer(),
                HolderLookup.Provider.create(Stream.empty())
        );
    }
}
