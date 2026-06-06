package me.RedEagle3.rankProgression.Tasks;

import me.RedEagle3.rankProgression.Managers.PlaytimeManager;
import me.RedEagle3.rankProgression.Managers.RankManager;
import me.RedEagle3.rankProgression.Managers.PlayerDataManager;
import me.RedEagle3.rankProgression.Models.RankMilestone;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import me.RedEagle3.rankProgression.Managers.LeaderboardManager;

import java.util.List;
import java.util.UUID;

public class  PromotionCheckTask extends BukkitRunnable {

    private final JavaPlugin plugin;
    private final PlaytimeManager playtimeManager;
    private final RankManager rankManager;
    private final PlayerDataManager playerDataManager;
    private final LeaderboardManager leaderboardManager;

    public PromotionCheckTask(JavaPlugin plugin,
                              PlaytimeManager playtimeManager,
                              RankManager rankManager,
                              PlayerDataManager playerDataManager,
                              LeaderboardManager leaderboardManager) {

        this.plugin = plugin;
        this.playtimeManager = playtimeManager;
        this.rankManager = rankManager;
        this.playerDataManager = playerDataManager;
        this.leaderboardManager = leaderboardManager;
    }

    @Override
    public void run() {
        String track = plugin.getConfig().getString("track-name");
        if (track == null) return;

        List<RankMilestone> milestones = rankManager.getMilestones();


        for (Player player : Bukkit.getOnlinePlayers()) {

            UUID uuid = player.getUniqueId();

            long playtime = playtimeManager.getPlaytimeMillis(uuid);

            int currentIndex = playerDataManager.getRankIndex(uuid);

            boolean firstTime = !playerDataManager.hasSeenPlayer(uuid);

            // MARK as seen immediately so we don't repeat bootstrap logic
            if (firstTime) {

                playerDataManager.markSeen(uuid);

                // 1. Find correct rank index from playtime
                int correctIndex = 0;

                for (RankMilestone m : milestones) {
                    if (playtime >= m.getRequiredTime()) {
                        correctIndex = m.getIndex();
                    } else {
                        break;
                    }
                }

                // 2. Save internal state
                playerDataManager.setRankIndex(uuid, correctIndex);

                // 3. Force-sync LuckPerms EXACTLY to correct position
                // (remove from track first to avoid drift)
                Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        "lp user " + player.getName() + " parent remove " + track
                );

                // 4. Rebuild rank step-by-step silently (no chat spam)
                for (int i = 0; i < correctIndex; i++) {
                    Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            "lp user " + player.getName() + " promote " + track
                    );
                }

                continue;
            }

            // NORMAL players (step-based progression)
            int nextIndex = currentIndex + 1;

            if (nextIndex >= milestones.size()) continue;

            RankMilestone next = milestones.get(nextIndex);

            if (playtime >= next.getRequiredTime()) {

                promotePlayer(player, next);

                playerDataManager.setRankIndex(uuid, nextIndex);
            }
        }

        // Refresh cached leaderboard
        leaderboardManager.rebuild();
    }

    private void promotePlayer(Player player, RankMilestone milestone) {

        String track = plugin.getConfig().getString("track-name");
        String command = "lp user %player% promote %track%";

        if (track == null || command == null) return;

        command = command
                .replace("%player%", player.getName())
                .replace("%track%", track);

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

        if (plugin.getConfig().getBoolean("broadcast-promotions")) {

            String msg = plugin.getConfig().getString("messages.broadcast")
                    .replace("%player%", player.getName())
                    .replace("%rank%", milestone.getRankName());

            Bukkit.broadcastMessage(msg.replace("&", "§"));
        }
    }
}