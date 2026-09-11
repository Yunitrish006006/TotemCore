package dev.totem.core.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;
import java.util.*;

/** World-local identities observed at login; no name lookup may contact Mojang. */
public final class TotemPlayerDirectoryData extends SavedData {
    public record Entry(UUID id, String name) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("id").forGetter(Entry::id),
                Codec.STRING.fieldOf("name").forGetter(Entry::name)).apply(i, Entry::new));
    }
    public static final Codec<TotemPlayerDirectoryData> CODEC = Entry.CODEC.listOf().xmap(TotemPlayerDirectoryData::new, TotemPlayerDirectoryData::entries);
    public static final SavedDataType<TotemPlayerDirectoryData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("totem", "known_players"), TotemPlayerDirectoryData::new,
            CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    private final Map<UUID, Entry> players = new HashMap<>();
    private List<Entry> sorted;
    public TotemPlayerDirectoryData() { }
    private TotemPlayerDirectoryData(List<Entry> entries) { entries.forEach(e -> players.put(e.id(), e)); }
    public static TotemPlayerDirectoryData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }
    public void remember(UUID id, String name) {
        Entry entry = new Entry(id, name == null || name.isBlank() ? id.toString() : name);
        if (!entry.equals(players.put(id, entry))) { sorted = null; setDirty(); }
    }
    public List<Entry> entries() {
        if (sorted == null) sorted = players.values().stream().sorted(Comparator.comparing(Entry::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Entry::id)).toList();
        return sorted;
    }
    public Optional<Entry> resolve(String value) {
        try { return Optional.ofNullable(players.get(UUID.fromString(value))); }
        catch (IllegalArgumentException ignored) { }
        var matches = players.values().stream().filter(e -> e.name().equalsIgnoreCase(value)).limit(2).toList();
        return matches.size() == 1 ? Optional.of(matches.getFirst()) : Optional.empty();
    }
    /** Reads only UUID filenames; the supplied resolver must consult local cached names only. */
    void importExisting(java.nio.file.Path directory, java.util.function.Function<UUID, Optional<String>> cachedName) {
        if (!Files.isDirectory(directory)) return;
        try (var files = Files.list(directory)) {
            files.filter(Files::isRegularFile).map(p -> p.getFileName().toString()).filter(n -> n.endsWith(".dat")).forEach(name -> {
                try {
                    UUID id = UUID.fromString(name.substring(0, name.length() - 4));
                    String display = cachedName.apply(id)
                            .orElseGet(() -> resolve(id.toString()).map(Entry::name).orElse(id.toString()));
                    remember(id, display);
                } catch (IllegalArgumentException ignored) { }
            });
        } catch (java.io.IOException error) {
            org.slf4j.LoggerFactory.getLogger("TotemCore").warn("Could not import existing player identities", error);
        }
    }
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                get(server).remember(handler.player.getUUID(), handler.player.getGameProfile().name()));
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            var data = get(server);
            data.importExisting(server.getWorldPath(LevelResource.PLAYER_DATA_DIR),
                    id -> server.services().nameToIdCache().get(id).map(NameAndId::name));
            server.getPlayerList().getPlayers().forEach(p -> data.remember(p.getUUID(), p.getGameProfile().name()));
        });
    }
}
