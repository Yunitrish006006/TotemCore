package dev.totem.core.player;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class TotemPlayerDirectoryDataTest {
    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }
    @Test void identitiesSurviveSaveAndReload(@TempDir Path directory) {
        UUID id = UUID.randomUUID();
        try (var storage = storage(directory)) {
            storage.computeIfAbsent(TotemPlayerDirectoryData.TYPE).remember(id,"OfflineAlice"); storage.saveAndJoin();
        }
        try (var storage = storage(directory)) {
            var known = storage.get(TotemPlayerDirectoryData.TYPE);
            assertEquals(id,known.resolve("offlinealice").orElseThrow().id());
            assertEquals("OfflineAlice",known.resolve(id.toString()).orElseThrow().name());
        }
    }
    @Test void renamesKeepUuidAndAmbiguousNamesFailClosed() {
        var known = new TotemPlayerDirectoryData(); UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        known.remember(first,"OldName"); known.remember(first,"NewName");
        assertTrue(known.resolve("OldName").isEmpty());
        assertEquals(first,known.resolve("newname").orElseThrow().id());
        known.remember(second,"NEWNAME");
        assertTrue(known.resolve("newname").isEmpty());
        assertTrue(known.resolve(UUID.randomUUID().toString()).isEmpty());
        assertEquals(first,known.resolve(first.toString()).orElseThrow().id());
    }
    @Test void importsOnlyExistingPlayerFilesAndKeepsNamesWithoutCache(@TempDir Path directory) throws Exception {
        var known = new TotemPlayerDirectoryData();
        UUID cached = UUID.randomUUID(), saved = UUID.randomUUID(), unknown = UUID.randomUUID(),
                notAFile = UUID.randomUUID(), cachedOnly = UUID.randomUUID();
        known.remember(saved, "SavedName");
        for (UUID id : List.of(cached, saved, unknown)) java.nio.file.Files.write(directory.resolve(id + ".dat"), new byte[0]);
        java.nio.file.Files.createDirectory(directory.resolve(notAFile + ".dat"));
        java.nio.file.Files.writeString(directory.resolve("invalid.dat"), "");
        java.nio.file.Files.writeString(directory.resolve(cachedOnly + ".dat_old"), "");
        var names = Map.of(cached, "CachedName", cachedOnly, "UnjoinedName");
        known.importExisting(directory, id -> Optional.ofNullable(names.get(id)));
        assertEquals(3, known.entries().size());
        assertEquals("CachedName", known.resolve(cached.toString()).orElseThrow().name());
        assertEquals("SavedName", known.resolve(saved.toString()).orElseThrow().name());
        assertEquals(unknown.toString(), known.resolve(unknown.toString()).orElseThrow().name());
        assertTrue(known.resolve(cachedOnly.toString()).isEmpty());
        assertTrue(known.resolve(notAFile.toString()).isEmpty());
    }
    private static SavedDataStorage storage(Path p) {
        return new SavedDataStorage(p,DataFixers.getDataFixer(),HolderLookup.Provider.create(Stream.empty()));
    }
}
