package me.RedEagle3.rankProgression.Commands;

import me.RedEagle3.rankProgression.Managers.LeaderboardManager;
import me.RedEagle3.rankProgression.Managers.PlayerDataManager;
import me.RedEagle3.rankProgression.Managers.PlaytimeManager;
import me.RedEagle3.rankProgression.Managers.RankManager;
import me.RedEagle3.rankProgression.Models.RankMilestone;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

public class SyncRankProgressionCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final PlaytimeManager playtimeManager;
    private final RankManager rankManager;
    private final PlayerDataManager playerDataManager;
    private final LeaderboardManager leaderboardManager;

    public SyncRankProgressionCommand(JavaPlugin plugin,
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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("rankprogress.admin")) {
            sender.sendMessage("§cYou do not have permission.");
            return true;
        }

        sender.sendMessage("§eStarting full rank progression sync...");

        new BukkitRunnable() {
            @Override
            public void run() {

                RankMilestone[] milestones = rankManager.getMilestones().toArray(new RankMilestone[0]);

                OfflinePlayer[] players = Bukkit.getOfflinePlayers();

                int updated = 0;

                for (OfflinePlayer player : players) {

                    if (player.getUniqueId() == null) continue;

                    long playtime = player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE) * 50L;

                    int correctIndex = 0;

                    for (RankMilestone milestone : milestones) {
                        if (playtime >= milestone.getRequiredMinutes()) {
                            correctIndex = milestone.getIndex();
                        } else {
                            break;
                        }
                    }

                    int currentIndex = playerDataManager.getRankIndex(player.getUniqueId());

                    if (correctIndex > currentIndex) {
                        playerDataManager.setRankIndex(player.getUniqueId(), correctIndex);
                        updated++;
                    }
                }

                playerDataManager.save();

                Bukkit.getScheduler().runTask(plugin, () -> {
                    leaderboardManager.rebuild();
                    sender.sendMessage("§aLeaderboard updated successfully.");
                });

                sender.sendMessage("§aSync complete! Updated players: §e" + updated);
            }
        }.runTaskAsynchronously(plugin);

        return true;
    }
}