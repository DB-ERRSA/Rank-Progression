package me.RedEagle3.rankProgression.Messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.RedEagle3.rankProgression.Commands.PlaytimeCommand;
import me.RedEagle3.rankProgression.Commands.ProgressionCommand;
import me.RedEagle3.rankProgression.GUI.LeaderboardGUI;
import me.RedEagle3.rankProgression.Managers.LeaderboardCacheManager;
import me.RedEagle3.rankProgression.Managers.RankManager;
import me.RedEagle3.rankProgression.Models.LeaderboardEntry;
import me.RedEagle3.rankProgression.Models.RankMilestone;
import me.RedEagle3.rankProgression.Utils.TextFormatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProxyMessageListener implements PluginMessageListener {

    private final JavaPlugin plugin;
    private final ProxyMessenger proxyMessenger;
    private final RankManager rankManager;
    private final PlaytimeCommand playtimeCommand;
    private final ProgressionCommand progressionCommand;
    private final LeaderboardCacheManager leaderboardCacheManager;
    private final LeaderboardGUI leaderboardGUI;

    public ProxyMessageListener(JavaPlugin plugin, ProxyMessenger proxyMessenger, RankManager rankManager, PlaytimeCommand playtimeCommand, ProgressionCommand progressionCommand, LeaderboardCacheManager leaderboardCacheManager, LeaderboardGUI leaderboardGUI) {
        this.plugin = plugin;
        this.proxyMessenger = proxyMessenger;
        this.rankManager = rankManager;
        this.playtimeCommand = playtimeCommand;
        this.progressionCommand = progressionCommand;
        this.leaderboardCacheManager = leaderboardCacheManager;
        this.leaderboardGUI = leaderboardGUI;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {

        if (!channel.equals("rankprogression:main")) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);

        String subChannel = in.readUTF();

        switch (subChannel) {

            case "INITIALIZE_PLAYER":
                initializePlayer(in);
                break;

            case "PROMOTION_RESULT":
                handlePromotionResult(in);
                break;

            case "RANK_DATA_RESPONSE":
                handleRankDataResponse(in);
                break;

            case "PLAYER_STATS_RESPONSE":
                handlePlayerStatsResponse(in);
                break;

            case "PLAYTIME_EXPORT_COMPLETE":
                handlePlaytimeExportComplete(in);
                break;

            case "LEADERBOARD_RESPONSE":
                handleLeaderboardResponse(in);
                break;

            default:
                plugin.getLogger().info("Unknown subchannel: " + subChannel);
                break;
        }
    }

    private void initializePlayer(ByteArrayDataInput in) {

        UUID uuid = UUID.fromString(in.readUTF());
        long totalPlaytime = in.readLong(); // TODO, not needed, only for log
        int rankIndex = in.readInt();

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        plugin.getLogger().info(player.getName() + " initialized at rank " + rankIndex + ", for total playtime: " + totalPlaytime);
        rankManager.assignRank(player, rankIndex);
        String rankLine = TextFormatter.getRankPrintLine(rankManager, rankIndex);
        if (rankIndex != -1) {
            player.sendMessage("§6Welcome back to the server, §b" + player.getName() + "§6! You have been promoted to " + rankLine + " §6based on your previous playtime.");
            player.sendMessage("§6Check out your stats with §b/playtime§6, or see your rank progress with §b/progression§6.");
        }
        proxyMessenger.playerInitialized(player, rankIndex);
    }

    private void handlePromotionResult(ByteArrayDataInput in) {

        UUID uuid = UUID.fromString(in.readUTF());
        boolean promoted = in.readBoolean();
        int rankIndex = in.readInt();
        String track = in.readUTF();

        if (!promoted) {return;}

        Player player = Bukkit.getPlayer(uuid);

        if (player == null) {return;}

        rankManager.promoteRank(player, rankIndex, track);

        String rankLine = TextFormatter.getRankPrintLine(rankManager, rankIndex);
        String promotionMessage = "§b"+ player.getName() + " §6has achieved " + rankLine + " §6rank!";
        boolean broadcastPromotions = plugin.getConfig().getBoolean("broadcast-promotions", true);

        if (broadcastPromotions) {
            Bukkit.broadcastMessage(promotionMessage);
        } else {
            player.sendMessage(promotionMessage);
        }

        proxyMessenger.playerPromoted(player, rankIndex);
    }

    public void handleRankDataResponse(ByteArrayDataInput in) {

        int count = in.readInt();

        System.out.println("TEMP: Received " + count + " ranks");

        rankManager.clearRanks();

        for (int i = 0; i < count; i++) {

            String name = in.readUTF();
            int index = in.readInt();
            long requiredMinutes = in.readLong();
            int rewardCount = in.readInt();   List<String> rewards = new ArrayList<>();   for (int j = 0; j < rewardCount; j++) {rewards.add(in.readUTF());}
            String icon = in.readUTF();
            String color = in.readUTF();

            rankManager.addRank(new RankMilestone(name, index, requiredMinutes, rewards, icon, color));
        }

        rankManager.setLoaded(true);

        for (UUID uuid : rankManager.getWaitingForRankData()) {

            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                proxyMessenger.playerJoin(player);
            }
        }
        rankManager.getWaitingForRankData().clear();

        // for (RankMilestone rank : rankManager.getMilestones()) {plugin.getLogger().info("TEMP:" + rank.getRankName() + " rewards: " + rank.getRewards());}
    }

    public void handlePlayerStatsResponse(ByteArrayDataInput in) {

        UUID uuid = UUID.fromString(in.readUTF());

        long totalMinutes = in.readLong();
        int rankIndex = in.readInt();
        long firstJoin = in.readLong();
        int joinCount = in.readInt();
        String reason = in.readUTF();

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        if (reason.equals("PLAYTIME_COMMAND")) {
            playtimeCommand.displayStats(player, totalMinutes, rankIndex, firstJoin, joinCount);
        } else if (reason.equals("PROGRESSION_COMMAND")) {
            progressionCommand.openProgression(player, totalMinutes, rankIndex);
        } else {
            System.out.println("ERROR: Unknown stats request type!");
            //logger.warn(Unknown stats request type!) // TODO: change above to logger
        }
    }

    private void handlePlaytimeExportComplete(ByteArrayDataInput in) {

        UUID uuid = UUID.fromString(in.readUTF());
        Player admin = Bukkit.getPlayer(uuid);
        String fileName = in.readUTF();
        int playerCount = in.readInt();

        if (!(admin == null)) {
            admin.sendMessage("§aExport complete! Saved §e" + playerCount + " §aplayers to §e" + fileName);
        }
    }

    public void handleLeaderboardResponse(ByteArrayDataInput in) {

        UUID requester = UUID.fromString(in.readUTF());

        // Viewer stats
        long totalMinutes = in.readLong();
        int rankIndex = in.readInt();
        long firstJoin = in.readLong();
        long lastSeen = in.readLong();
        int joinCount = in.readInt();
        boolean online = in.readBoolean();
        String serverName = in.readUTF();

        Player viewer = Bukkit.getPlayer(requester);
        if (viewer == null) {
            return;
        }

        LeaderboardEntry viewerData = new LeaderboardEntry(requester, viewer.getName(), totalMinutes, rankIndex, firstJoin, lastSeen, joinCount, online, serverName);

        int size = in.readInt();

        List<LeaderboardEntry> leaderboard = new ArrayList<>();

        for (int i = 0; i < size; i++) {

            UUID uuid = UUID.fromString(in.readUTF());
            String username = in.readUTF();
            long minutes = in.readLong();
            int rank = in.readInt();
            long first = in.readLong();
            long last = in.readLong();
            int joins = in.readInt();
            boolean isOnline = in.readBoolean();
            String server = in.readUTF();

            leaderboard.add(new LeaderboardEntry(uuid, username, minutes, rank, first, last, joins, isOnline, server));
        }

        leaderboardCacheManager.updateLeaderboard(leaderboard);
        leaderboardGUI.open(viewer, viewerData);
    }
}