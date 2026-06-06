package me.RedEagle3.rankProgression.Managers;

import me.RedEagle3.rankProgression.Messaging.ProxyMessenger;
import me.RedEagle3.rankProgression.Utils.TextFormatter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

// TODO: Delete this class, it is not used
public class PlayerInitializationManager {

    private final JavaPlugin plugin;
    private final RankManager rankManager;
    private final ProxyMessenger proxyMessenger;

    public PlayerInitializationManager(JavaPlugin plugin, RankManager rankManager, ProxyMessenger proxyMessenger) {
        this.plugin = plugin;
        this.rankManager = rankManager;
        this.proxyMessenger = proxyMessenger;
    }

    public void initializePlayer(Player player, long totalPlaytime, int rankIndex) {

        plugin.getLogger().info(player.getName() + " initialized at rank " + rankIndex + ", for total playtime: " + totalPlaytime);
        rankManager.assignRank(player, rankIndex);
        String rankLine = TextFormatter.getRankPrintLine(rankManager, rankIndex);
        if (rankIndex != -1) { player.sendMessage("§6Welcome back to the server, §b" + player.getName() + "§6! You have been promoted to " + rankLine + " §6based on your previous playtime."); }
        proxyMessenger.playerInitialized(player, rankIndex);
    }
}