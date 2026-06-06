package me.RedEagle3.rankProgression.Commands;

import me.RedEagle3.rankProgression.GUI.LeaderboardGUI;
import me.RedEagle3.rankProgression.Managers.PlayerDataManager;
import me.RedEagle3.rankProgression.Managers.PlaytimeManager;
import me.RedEagle3.rankProgression.Managers.RankManager;
import me.RedEagle3.rankProgression.Messaging.ProxyMessenger;
import me.RedEagle3.rankProgression.Utils.TextFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PlaytimeCommand implements CommandExecutor {

    private final PlaytimeManager playtimeManager;
    private final LeaderboardGUI leaderboardGUI;
    private final PlayerDataManager playerDataManager;
    private final RankManager rankManager;
    private final ProxyMessenger proxyMessenger;

    public PlaytimeCommand(PlaytimeManager playtimeManager, LeaderboardGUI leaderboardGUI, PlayerDataManager playerDataManager, RankManager rankManager, ProxyMessenger proxyMessenger) {
        this.playtimeManager = playtimeManager;
        this.leaderboardGUI = leaderboardGUI;
        this.playerDataManager = playerDataManager;
        this.rankManager = rankManager;
        this.proxyMessenger = proxyMessenger;
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

        proxyMessenger.requestPlayerStats(player);

        return true;
    }

    public void displayStats(Player player, long totalMinutes, int rankIndex, long firstJoin, int joinCount) {

        long hours = totalMinutes / 60;
        long days = hours / 24;
        long displayHours = hours % 24;
        long displayMinutes = totalMinutes % 60;

        String firstJoinDate = new SimpleDateFormat("MMM dd, yyyy").format(new Date(firstJoin));

        String rankLine = "§7Unknown";
        if (rankIndex >= 0 && rankIndex < rankManager.getMilestones().size()) {
            rankLine = TextFormatter.getRankPrintLine(rankManager, rankIndex);
        }

        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§e§lPlaytime Statistics");
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§eTotal Playtime: §f" + days + "d " + displayHours + "h " + displayMinutes + "m");
        player.sendMessage("§eCurrent Rank: " + rankLine);
        player.sendMessage("§eFirst Joined: §f" + firstJoinDate);
        player.sendMessage("§eTimes Joined: §f" + joinCount);
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━");
    }
}