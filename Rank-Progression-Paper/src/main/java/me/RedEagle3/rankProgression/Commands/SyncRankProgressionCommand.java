package me.RedEagle3.rankProgression.Commands;

import me.RedEagle3.rankProgression.Messaging.ProxyMessenger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SyncRankProgressionCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final ProxyMessenger proxyMessenger;

    public SyncRankProgressionCommand(JavaPlugin plugin, ProxyMessenger proxyMessenger) {
        this.plugin = plugin;
        this.proxyMessenger = proxyMessenger;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player admin)) {
            sender.sendMessage("This command can only be used in-game.");
            return true;
        }

        if (!sender.hasPermission("rankprogression.syncrankprogression")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        sender.sendMessage("§eStarting offline player sync...");

        OfflinePlayer[] players = Bukkit.getOfflinePlayers();

        int synced = 0;

        String serverName = plugin.getConfig().getString("server-name");
        if (serverName == null || serverName.isBlank()) {serverName = "unknown";}

        for (OfflinePlayer player : players) {

            if (player.isOnline()) {
                continue;
            }

            long playtimeMinutes = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 60;
            long firstJoin = player.getFirstPlayed();
            int joinCount = player.getStatistic(Statistic.LEAVE_GAME) + 1;
            long lastOnline = player.getLastLogin();

            proxyMessenger.syncOfflinePlayer(admin, player.getUniqueId(), player.getName(), playtimeMinutes, serverName, firstJoin, joinCount, lastOnline);

            synced++;
        }

        sender.sendMessage("§aSync complete! Sent §e" + synced + "§a players.");

        return true;
    }
}