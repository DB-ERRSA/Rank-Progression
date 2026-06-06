package me.RedEagle3.rankProgression.Managers;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlaytimeManager {

    /**
     * Gets total playtime in milliseconds for an ONLINE player.
     */
    public long getPlaytimeMillis(UUID uuid) {

        OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);

        if (offline.isOnline()) {
            Player player = offline.getPlayer();
            return player.getStatistic(Statistic.PLAY_ONE_MINUTE) * 50L;
        }

        // fallback for offline players
        return offline.getStatistic(Statistic.PLAY_ONE_MINUTE) * 50L;
    }

    /**
     * Gets playtime in seconds.
     */
    public long getPlaytimeSeconds(Player player) {
        return getPlaytimeMillis(player.getUniqueId()) / 1000L;
    }

    /**
     * Gets playtime in minutes.
     */
    public long getLocalPlaytimeMinutes(Player player) {
        return getPlaytimeSeconds(player) / 60L;
    }

    /**
     * Gets playtime in hours (rounded down).
     */
    public long getPlaytimeHours(Player player) {
        return getLocalPlaytimeMinutes(player) / 60L;
    }
}