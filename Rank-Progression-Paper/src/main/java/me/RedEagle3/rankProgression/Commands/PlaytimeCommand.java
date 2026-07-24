package me.RedEagle3.rankProgression.Commands;

import me.RedEagle3.rankProgression.Managers.RankManager;
import me.RedEagle3.rankProgression.Messaging.ProxyMessenger;
import me.RedEagle3.rankProgression.Utils.TextFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PlaytimeCommand implements CommandExecutor, TabCompleter {

    private final RankManager rankManager;
    private final ProxyMessenger proxyMessenger;

    public PlaytimeCommand(RankManager rankManager, ProxyMessenger proxyMessenger) {
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

                proxyMessenger.requestLeaderboard(player);
                return true;
            }
        }

        proxyMessenger.requestPlayerStats(player, "PLAYTIME_COMMAND");

        return true;
    }

    public void displayStats(Player player, long totalMinutes, int rankIndex, long firstJoin, int joinCount, boolean isZenith) {

        long hours = totalMinutes / 60;
        long days = hours / 24;
        long displayHours = hours % 24;
        long displayMinutes = totalMinutes % 60;

        String firstJoinDate = new SimpleDateFormat("MMM dd, yyyy").format(new Date(firstJoin));

        if (isZenith) {
            rankIndex = rankManager.getMilestones().size()-1;
        }

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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (args.length == 1) {

            List<String> completions = new ArrayList<>();

            if ("top".startsWith(args[0].toLowerCase())) {
                completions.add("top");
            }

            if ("leaderboard".startsWith(args[0].toLowerCase())) {
                completions.add("leaderboard");
            }

            return completions;
        }

        return Collections.emptyList();
    }
}