package me.RedEagle3.rankProgression.Managers;

import me.RedEagle3.rankProgression.Models.LeaderboardEntry;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardManager {

    private final JavaPlugin plugin; // TODO Old as well, probably can delete

    private List<LeaderboardEntry> leaderboard = new ArrayList<>();

    public LeaderboardManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void rebuild() {

        List<LeaderboardEntry> list = new ArrayList<>();

        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {

            long playtime = player.getStatistic(Statistic.PLAY_ONE_MINUTE) * 50L;

            //list.add(new LeaderboardEntry(player.getUniqueId(), player.getName(), playtime, 0));
        }

        leaderboard = list.stream()
                .sorted((a, b) -> Long.compare(b.getTotalMinutes(), a.getTotalMinutes()))
                .limit(27)
                .collect(Collectors.toList());
    }

    public List<LeaderboardEntry> getTop() {
        return leaderboard;
    }
}