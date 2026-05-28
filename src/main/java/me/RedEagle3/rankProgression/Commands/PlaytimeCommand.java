package me.RedEagle3.rankProgression.Commands;

import me.RedEagle3.rankProgression.GUI.LeaderboardGUI;
import me.RedEagle3.rankProgression.Managers.PlayerDataManager;
import me.RedEagle3.rankProgression.Managers.PlaytimeManager;
import me.RedEagle3.rankProgression.Managers.RankManager;
import me.RedEagle3.rankProgression.Models.RankMilestone;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PlaytimeCommand implements CommandExecutor {

    private final PlaytimeManager playtimeManager;
    private final LeaderboardGUI leaderboardGUI;
    private  final PlayerDataManager playerDataManager;
    private  final RankManager rankManager;

    public PlaytimeCommand(PlaytimeManager playtimeManager,
                           LeaderboardGUI leaderboardGUI, PlayerDataManager playerDataManager, RankManager rankManager) {
        this.playtimeManager = playtimeManager;
        this.leaderboardGUI = leaderboardGUI;
        this.playerDataManager = playerDataManager;
        this.rankManager = rankManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used in-game.");
            return true;
        }

        if (args.length > 0) {

            String sub = args[0].toLowerCase();

            if (sub.equals("top") || sub.equals("leaderboard")) {

                leaderboardGUI.open(player);
                return true;
            }
        }

        // === PLAYTIME ===
        long millis = playtimeManager.getPlaytimeMillis(player.getUniqueId());

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        long displayHours = hours % 24;
        long displayMinutes = minutes % 60;

        // === FIRST JOIN ===
        long firstPlayed = player.getFirstPlayed();
        String firstJoin = new SimpleDateFormat("MMM dd, yyyy").format(new Date(firstPlayed));

        // === HEADER ===
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§e§lPlaytime Statistics");
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━");

        // === PLAYTIME ===
        player.sendMessage("§eTotal Playtime: §f" + days + "d " + displayHours + "h " + displayMinutes + "m");

        // === RANK INFO ===
        int rankIndex = playerDataManager.getRankIndex(player.getUniqueId());

        String rankLine = "§7Unknown";

        if (rankIndex >= 0 && rankIndex < rankManager.getMilestones().size()) {

            RankMilestone milestone =
                    rankManager.getMilestones().get(rankIndex);

            String color = color(milestone.getColor());
            String name = capitalize(milestone.getRankName());

            rankLine = "§7[" + color + name + "§7]";
        }

        player.sendMessage("§eCurrent Rank: " + rankLine);

        // === FIRST JOIN ===
        player.sendMessage("§eFirst Joined: §f" + firstJoin);

        // === JOIN COUNT ===
        int joins = player.getStatistic(org.bukkit.Statistic.LEAVE_GAME) + 1;

        player.sendMessage("§eTimes Joined: §f" + joins);

        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━");

        return true;
    }

    private String capitalize(String s) {

        if (s == null || s.isEmpty()) return s;

        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String color(String text) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
    }
}