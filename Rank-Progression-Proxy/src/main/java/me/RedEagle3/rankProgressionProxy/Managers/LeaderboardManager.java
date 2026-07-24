package me.RedEagle3.rankProgressionProxy.Managers;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.RedEagle3.rankProgressionProxy.Models.LeaderboardEntry;

import java.util.*;

public class LeaderboardManager {

    private final PlaytimeDataManager playtimeDataManager;
    private final ProxyServer server;
    private List<LeaderboardEntry> cache = new ArrayList<>();

    public LeaderboardManager(ProxyServer server, PlaytimeDataManager playtimeDataManager) {
        this.server = server;
        this.playtimeDataManager = playtimeDataManager;
    }

    public void rebuild() {

        List<LeaderboardEntry> entries = new ArrayList<>();

        for (UUID uuid : playtimeDataManager.getAllPlayers()) {

            boolean online = false;
            String serverName = "Offline";

            Optional<Player> player = server.getPlayer(uuid);

            if (player.isPresent()) {
                online = true;
                serverName = player.get().getCurrentServer().map(connection -> connection.getServerInfo().getName()).orElse("Unknown");
            }

            entries.add(
                    new LeaderboardEntry(
                            uuid,
                            playtimeDataManager.getUsername(uuid),
                            playtimeDataManager.getTotalPlaytime(uuid),
                            playtimeDataManager.getRankIndex(uuid),
                            playtimeDataManager.getFirstJoin(uuid),
                            playtimeDataManager.getLastSeen(uuid),
                            playtimeDataManager.getJoinCount(uuid),
                            playtimeDataManager.hasZenith(uuid),
                            online,
                            serverName
                    )
            );
        }

        entries.sort(
                Comparator.comparingLong(
                        LeaderboardEntry::getTotalMinutes
                ).reversed()
        );

        if (entries.size() > 28) {
            entries = new ArrayList<>(entries.subList(0, 28));
        }

        cache = entries;
    }

    public List<LeaderboardEntry> getCache() {
        return cache;
    }
}