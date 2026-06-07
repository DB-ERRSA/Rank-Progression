package me.RedEagle3.rankProgression;


import me.RedEagle3.rankProgression.Commands.PlaytimeCommand;
import me.RedEagle3.rankProgression.Commands.ProgressionCommand;
import me.RedEagle3.rankProgression.Commands.ExportPlaytimeDataCommand;
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

    @Override
    public void onEnable() {

        saveDefaultConfig();

        // === MANAGERS ===
        PlaytimeManager playtimeManager = new PlaytimeManager();
        RankManager rankManager = new RankManager(this);
        PlayerDataManager playerDataManager = new PlayerDataManager(this);
        ProxyMessenger proxyMessenger = new ProxyMessenger(this, playtimeManager);
        LeaderboardManager leaderboardManager = new LeaderboardManager(this);

        // === GUIS ===
        LeaderboardGUI leaderboardGUI = new LeaderboardGUI(leaderboardManager, playtimeManager, rankManager, playerDataManager);
        ProgressionGUI progressionGUI = new ProgressionGUI(rankManager);

        // === COMMANDS ===
        PlaytimeCommand playtimeCommand = new PlaytimeCommand(playtimeManager, leaderboardGUI, playerDataManager, rankManager, proxyMessenger);
        ProgressionCommand progressionCommand = new ProgressionCommand(progressionGUI, proxyMessenger);
        SyncRankProgressionCommand syncRankProgressionCommand = new SyncRankProgressionCommand(this, proxyMessenger);
        ExportPlaytimeDataCommand exportPlaytimeDataCommand = new ExportPlaytimeDataCommand(proxyMessenger);

        // === COMMAND EXECUTERS ===
        getCommand("playtime").setExecutor(playtimeCommand);
        getCommand("progression").setExecutor(progressionCommand);
        getCommand("syncrankprogression").setExecutor(syncRankProgressionCommand);
        getCommand("exportplaytimedata").setExecutor(exportPlaytimeDataCommand);

        // === LISTENERS ===
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, proxyMessenger, rankManager), this);

        // === PROXY SETUP ===
        getServer().getMessenger().registerIncomingPluginChannel(this, "rankprogression:main", new ProxyMessageListener(this, proxyMessenger, rankManager, playtimeCommand, progressionCommand));
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "rankprogression:main");

        // initial leaderboard build
        leaderboardManager.rebuild();

        // === TASKS ===
        long interval = getConfig().getLong("check-interval", 5);
        new MainLoop(proxyMessenger).runTaskTimer(this, 20L * 60 * interval, 20L * 60 * interval);

        getLogger().info("RankProgression enabled successfully!");
    }
}
