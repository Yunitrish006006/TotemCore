package dev.totem.core.api.v1.player;

import dev.totem.core.player.TotemPlayerDirectoryData;
import net.minecraft.server.MinecraftServer;
import java.util.*;

/**
 * Read-only server-thread API for identities that actually joined this world.
 * Names are last seen names, UUIDs are identity. Ambiguous names fail closed.
 * No operation performs network profile lookups. Callers own authorization and request budgets;
 * only expose results after checking the requesting player's permission for their feature.
 */
public final class TotemPlayerDirectoryApi {
    public static final int MAX_QUERY_LENGTH = 64;
    public static final int MAX_PAGE_SIZE = 64;
    public record KnownPlayer(UUID id, String name, boolean online) { }
    public record Page(int page, int totalPages, int totalPlayers, List<KnownPlayer> players) {
        public Page { players = List.copyOf(players); }
    }
    private TotemPlayerDirectoryApi() { }
    public static Optional<KnownPlayer> find(MinecraftServer server, UUID id) {
        return find(server, Objects.requireNonNull(id).toString());
    }
    /** Accepts an exact UUID or unambiguous case-insensitive last-seen name; never invents an offline UUID. */
    public static Optional<KnownPlayer> find(MinecraftServer server, String nameOrId) {
        checkThread(server);
        if (nameOrId == null || nameOrId.length() > MAX_QUERY_LENGTH) return Optional.empty();
        return TotemPlayerDirectoryData.get(server).resolve(nameOrId.trim()).map(e -> view(server,e));
    }
    public static Page search(MinecraftServer server, String query, int page, int pageSize) {
        return search(server,query,page,pageSize,Set.of());
    }
    /** Exclusions are server-owned identities (for example, a resource owner who cannot receive another role). */
    public static Page search(MinecraftServer server, String query, int page, int pageSize, Set<UUID> excluded) {
        checkThread(server);
        if (query == null || query.length() > MAX_QUERY_LENGTH || page < 0 || pageSize < 1 || pageSize > MAX_PAGE_SIZE)
            throw new IllegalArgumentException("Invalid player directory query bounds");
        var omitted = Set.copyOf(excluded);
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        var matches = TotemPlayerDirectoryData.get(server).entries().stream().filter(e -> !omitted.contains(e.id()))
                .filter(e -> e.name().toLowerCase(Locale.ROOT).contains(normalized) || e.id().toString().contains(normalized)).toList();
        int totalPages = Math.max(1,(matches.size()+pageSize-1)/pageSize);
        int selectedPage = Math.min(page,totalPages-1);
        return new Page(selectedPage,totalPages,matches.size(),matches.stream().skip((long)selectedPage*pageSize)
                .limit(pageSize).map(e -> view(server,e)).toList());
    }
    private static KnownPlayer view(MinecraftServer server, TotemPlayerDirectoryData.Entry entry) {
        return new KnownPlayer(entry.id(),entry.name(),server.getPlayerList().getPlayer(entry.id()) != null);
    }
    private static void checkThread(MinecraftServer server) {
        if (!Objects.requireNonNull(server).isSameThread()) throw new IllegalStateException("Player directory requires the server thread");
    }
}
