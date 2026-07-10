package me.RedEagle3.rankProgression;

import me.RedEagle3.rankProgression.Commands.ExportPlaytimeDataCommand;
import me.RedEagle3.rankProgression.Commands.PlaytimeCommand;
import me.RedEagle3.rankProgression.Commands.ProgressionCommand;
import me.RedEagle3.rankProgression.Commands.SyncRankProgressionCommand;
import me.RedEagle3.rankProgression.GUI.LeaderboardGUI;
import me.RedEagle3.rankProgression.GUI.ProgressionGUI;
import me.RedEagle3.rankProgression.Listeners.GUIListener;
import me.RedEagle3.rankProgression.Listeners.PlayerJoinListener;
import me.RedEagle3.rankProgression.Managers.LeaderboardCacheManager;
import me.RedEagle3.rankProgression.Managers.RankManager;
import me.RedEagle3.rankProgression.Messaging.ProxyMessageListener;
import me.RedEagle3.rankProgression.Messaging.ProxyMessenger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class RankProgression extends JavaPlugin {

    @Override
    public void onEnable() {

        saveDefaultConfig();

        // === MANAGERS ===
        RankManager rankManager = new RankManager(this);
        ProxyMessenger proxyMessenger = new ProxyMessenger(this);
        LeaderboardCacheManager leaderboardCacheManager = new LeaderboardCacheManager();

        // === GUIS ===
        LeaderboardGUI leaderboardGUI = new LeaderboardGUI(leaderboardCacheManager, rankManager);
        ProgressionGUI progressionGUI = new ProgressionGUI(rankManager);

        // === COMMANDS ===
        PlaytimeCommand playtimeCommand = new PlaytimeCommand(rankManager, proxyMessenger);
        ProgressionCommand progressionCommand = new ProgressionCommand(progressionGUI, proxyMessenger);
        SyncRankProgressionCommand syncRankProgressionCommand = new SyncRankProgressionCommand(this, proxyMessenger);
        ExportPlaytimeDataCommand exportPlaytimeDataCommand = new ExportPlaytimeDataCommand(proxyMessenger);

        // === COMMAND EXECUTERS ===
        getCommand("playtime").setExecutor(playtimeCommand);
        getCommand("playtime").setTabCompleter(playtimeCommand);
        getCommand("progression").setExecutor(progressionCommand);
        getCommand("syncrankprogression").setExecutor(syncRankProgressionCommand);
        getCommand("exportplaytimedata").setExecutor(exportPlaytimeDataCommand);

        // === LISTENERS ===
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, proxyMessenger, rankManager), this);

        // === PROXY SETUP ===
        getServer().getMessenger().registerIncomingPluginChannel(this, "rankprogression:main", new ProxyMessageListener(this, proxyMessenger, rankManager, playtimeCommand, progressionCommand, leaderboardCacheManager, leaderboardGUI));
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "rankprogression:main");

        // === TASKS ===
        long interval = getConfig().getLong("check-interval", 1);
        boolean allowPromotions = getConfig().getBoolean("allow-promotions", false);
        mainLoop(proxyMessenger, allowPromotions).runTaskTimer(this, 20L * 60 * interval, 20L * 60 * interval);

        getLogger().info("RankProgression enabled successfully!");
    }

    private BukkitRunnable mainLoop(ProxyMessenger proxyMessenger, boolean allowPromotions) {

        return new BukkitRunnable() {

            @Override
            public void run() {

                for (Player player : Bukkit.getOnlinePlayers()) {

                    if (!player.hasPermission("rankprogression.autopromote")) {
                        continue;
                    }

                    proxyMessenger.updatePlaytime(player);

                    if (allowPromotions) {
                        proxyMessenger.checkPromotion(player);
                        // proxyMessenger.checkZenith(player); TODO: Implement
                    }
                }
            }

        };
    }
}