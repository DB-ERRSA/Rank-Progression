package me.RedEagle3.rankProgression;


import me.RedEagle3.rankProgression.Commands.PlaytimeCommand;
import me.RedEagle3.rankProgression.Commands.ProgressionCommand;
import me.RedEagle3.rankProgression.Commands.SavePlaytimeDataCommand;
import me.RedEagle3.rankProgression.Commands.SyncRankProgressionCommand;
import me.RedEagle3.rankProgression.GUI.LeaderboardGUI;
import me.RedEagle3.rankProgression.GUI.ProgressionGUI;
import me.RedEagle3.rankProgression.Listeners.GUIListener;
import me.RedEagle3.rankProgression.Listeners.PlayerJoinListener;
import me.RedEagle3.rankProgression.Managers.*;
import me.RedEagle3.rankProgression.Messaging.ProxyMessageListener;
import me.RedEagle3.rankProgression.Messaging.ProxyMessenger;
import me.RedEagle3.rankProgression.Tasks.MainLoop;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class RankProgression extends JavaPlugin {

    private PlaytimeManager playtimeManager;
    private RankManager rankManager;
    private PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        // === CORE SYSTEMS ===
        PlaytimeManager playtimeManager = new PlaytimeManager();
        RankManager rankManager = new RankManager(this);
        PlayerDataManager playerDataManager = new PlayerDataManager(this);
        ProgressionGUI progressionGUI = new ProgressionGUI(this, rankManager, playerDataManager, playtimeManager);
        SavePlaytimeDataCommand savePlaytimeDataCommand = new SavePlaytimeDataCommand(this, playtimeManager, playerDataManager);
        ProxyMessenger proxyMessenger = new ProxyMessenger(this, playtimeManager);
        PlayerInitializationManager initializationManager = new PlayerInitializationManager(this, rankManager, proxyMessenger);

        // === LEADERBOARD SYSTEM ===
        LeaderboardManager leaderboardManager = new LeaderboardManager(this);
        LeaderboardGUI leaderboardGUI = new LeaderboardGUI(leaderboardManager, playtimeManager, rankManager, playerDataManager);

        // === COMMANDS ===
        PlaytimeCommand playtimeCommand = new PlaytimeCommand(playtimeManager, leaderboardGUI, playerDataManager, rankManager, proxyMessenger);
        getCommand("playtime").setExecutor(playtimeCommand);
        getCommand("syncrankprogression").setExecutor(new SyncRankProgressionCommand(this, playtimeManager, rankManager, playerDataManager, leaderboardManager));
        getCommand("progression").setExecutor(new ProgressionCommand(progressionGUI));
        getCommand("saveplaytimedata").setExecutor(savePlaytimeDataCommand);

        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, proxyMessenger, rankManager), this);

        getServer().getMessenger().registerIncomingPluginChannel(this, "rankprogression:main", new ProxyMessageListener(this, proxyMessenger, initializationManager, rankManager, playtimeCommand));
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "rankprogression:main");

        // === STARTUP SYNC ===
        syncOnlinePlayers();

        // initial leaderboard build
        leaderboardManager.rebuild();

        // === TASKS ===
        long interval = getConfig().getLong("check-interval", 5);
        new MainLoop(proxyMessenger).runTaskTimer(this, 20L * 60 * interval, 20L * 60 * interval);

        getLogger().info("RankProgression enabled successfully!");
    }

    @Override
    public void onDisable() {

        // Save player data safely
        if (playerDataManager != null) {
            playerDataManager.save();
        }

        getLogger().info("RankProgression disabled.");
    }

    // Optional getters (useful later for expansions)
    public PlaytimeManager getPlaytimeManager() {
        return playtimeManager;
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    private void syncOnlinePlayers() {

        getServer().getOnlinePlayers().forEach(player -> {

            long playtime = playtimeManager.getPlaytimeMillis(player.getUniqueId());

            playerDataManager.syncPlayer(
                    player.getUniqueId(),
                    playtime,
                    rankManager.getMilestones()
            );
        });

        getLogger().info("RankProgression: Online player sync completed.");
    }
}
