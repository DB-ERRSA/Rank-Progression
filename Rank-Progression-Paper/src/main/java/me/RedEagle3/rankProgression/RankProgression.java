package me.RedEagle3.rankProgression;


import me.RedEagle3.rankProgression.Commands.*;
import me.RedEagle3.rankProgression.GUI.LeaderboardGUI;
import me.RedEagle3.rankProgression.GUI.ProgressionGUI;
import me.RedEagle3.rankProgression.Listeners.GUIListener;
import me.RedEagle3.rankProgression.Listeners.TestMessageListener;
import me.RedEagle3.rankProgression.Managers.LeaderboardManager;
import me.RedEagle3.rankProgression.Managers.PlayerDataManager;
import me.RedEagle3.rankProgression.Managers.PlaytimeManager;
import me.RedEagle3.rankProgression.Managers.RankManager;
import me.RedEagle3.rankProgression.Tasks.PromotionCheckTask;
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

        // === LEADERBOARD SYSTEM ===
        LeaderboardManager leaderboardManager = new LeaderboardManager(this);
        LeaderboardGUI leaderboardGUI = new LeaderboardGUI(leaderboardManager, playtimeManager, rankManager, playerDataManager);

        // === COMMANDS ===
        getCommand("playtime").setExecutor(new PlaytimeCommand(playtimeManager, leaderboardGUI, playerDataManager, rankManager));
        getCommand("syncrankprogression").setExecutor(new SyncRankProgressionCommand(this, playtimeManager, rankManager, playerDataManager, leaderboardManager));
        getCommand("progression").setExecutor(new ProgressionCommand(progressionGUI));
        getCommand("saveplaytimedata").setExecutor(savePlaytimeDataCommand);

        getServer().getPluginManager().registerEvents(new GUIListener(), this);

        // === STARTUP SYNC ===
        syncOnlinePlayers();

        // === TASK ===
        long interval = getConfig().getLong("check-interval", 5);

        new PromotionCheckTask(this, playtimeManager, rankManager, playerDataManager, leaderboardManager
        ).runTaskTimer(this, 20L * 60 * interval, 20L * 60 * interval);

        // initial leaderboard build
        leaderboardManager.rebuild();

        // TODO TEMP, DELETE LATER
        // TODO TEMP, DELETE LATER
        getCommand("ptest").setExecutor(new ProxyTestCommand(this));
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "rankprogression:main");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "rankprogression:main", (ch, player, msg) -> {});
        Bukkit.getPluginManager().registerEvents(new TestMessageListener(this), this);

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
