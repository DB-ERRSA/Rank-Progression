package me.RedEagle3.rankProgressionProxy.Listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import me.RedEagle3.rankProgressionProxy.Managers.LeaderboardManager;
import me.RedEagle3.rankProgressionProxy.Managers.PlaytimeDataManager;
import me.RedEagle3.rankProgressionProxy.Managers.RankDataManager;
import me.RedEagle3.rankProgressionProxy.Models.LeaderboardEntry;
import me.RedEagle3.rankProgressionProxy.Models.RankData;
import me.RedEagle3.rankProgressionProxy.RankProgressionProxy;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;


public class PluginMessageListener {

    private final RankProgressionProxy plugin;
    private static final MinecraftChannelIdentifier CHANNEL = MinecraftChannelIdentifier.from("rankprogression:main");
    private final PlaytimeDataManager playtimeDataManager;
    private final RankDataManager rankDataManager;
    private final LeaderboardManager leaderboardManager;

    public PluginMessageListener(RankProgressionProxy plugin, PlaytimeDataManager playtimeDataManager, RankDataManager rankDataManager, LeaderboardManager leaderboardManager) {
        this.plugin = plugin;
        this.playtimeDataManager = playtimeDataManager;
        this.rankDataManager = rankDataManager;
        this.leaderboardManager = leaderboardManager;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {

        if (!event.getIdentifier().equals(CHANNEL)) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());

        String subChannel = in.readUTF();

        switch (subChannel) {

            case "UPDATE_PLAYTIME":
                handleUpdatePlaytime(in);
                break;

            case "PLAYER_JOIN":
                handlePlayerJoin(in, event);
                break;

            case "PLAYER_INITIALIZED":
                handlePlayerInitialized(in);
                break;

            case "CHECK_PROMOTION":
                handleCheckPromotion(in, event);
                break;

            case "CHECK_ZENITH_PROMOTION":
                handleCheckZenithPromotion(in, event);
                break;

            case "PLAYER_PROMOTED":
                handlePlayerPromoted(in);
                break;

            case "PLAYER_ZENITH_PROMOTED":
                handlePlayerZenithPromoted(in);
                break;

            case "REQUEST_RANK_DATA":
                handleRankDataRequest(event);
                break;

            case "REQUEST_PLAYER_STATS":
                handlePlayerStatsRequest(in, event);
                break;

            case "OFFLINE_SYNC_PLAYER_DATA":
                handleOfflineSyncPlayerData(in);
                break;

            case "REQUEST_PLAYTIME_EXPORT":
                handlePlaytimeExportRequest(in, event);
                break;

            case "REQUEST_LEADERBOARD":
                handleLeaderboardRequest(in, event);
                break;

            default:
                System.out.println("Unknown subchannel: " + subChannel);
                break;
        }
    }

    private void handleUpdatePlaytime(ByteArrayDataInput in) {

        UUID uuid = UUID.fromString(in.readUTF());
        String server = in.readUTF();
        long minutes = in.readLong();

        playtimeDataManager.updateServerPlaytime(uuid, server, minutes);
    }

    private void handlePlayerJoin(ByteArrayDataInput in, PluginMessageEvent event) {

        UUID uuid = UUID.fromString(in.readUTF());
        String username = in.readUTF();
        String server = in.readUTF();
        long minutes = in.readLong();
        long firstPlayed = in.readLong();
        int joinCount = in.readInt();

        playtimeDataManager.setUsername(uuid, username);

        boolean initialized = playtimeDataManager.isPlayerInitialized(uuid);

        if (!initialized) {

            playtimeDataManager.updateServerPlaytime(uuid, server, minutes);
            playtimeDataManager.setFirstJoin(uuid, firstPlayed);
            playtimeDataManager.setJoinCount(uuid, joinCount);

            long totalPlaytime = playtimeDataManager.getTotalPlaytime(uuid);
            int rankIndex = rankDataManager.getRankIndexForPlaytime(totalPlaytime);

            playtimeDataManager.setRankIndex(uuid, rankIndex);

            ByteArrayDataOutput out = ByteStreams.newDataOutput();

            out.writeUTF("INITIALIZE_PLAYER");
            out.writeUTF(uuid.toString());
            out.writeLong(totalPlaytime);
            out.writeInt(rankIndex);

            ServerConnection serverConnection = (ServerConnection) event.getSource();
            serverConnection.sendPluginMessage(MinecraftChannelIdentifier.from("rankprogression:main"), out.toByteArray());
        }
    }

    private void handlePlayerInitialized(ByteArrayDataInput in) {

        UUID uuid = UUID.fromString(in.readUTF());
        int rankIndex = in.readInt();

        playtimeDataManager.setPlayerInitialized(uuid, true);

        System.out.println("Initialized " + uuid + " at rank " + rankIndex);
    }

    private void handleCheckPromotion(ByteArrayDataInput in, PluginMessageEvent event) {

        UUID uuid = UUID.fromString(in.readUTF());
        int currentRank = playtimeDataManager.getRankIndex(uuid);
        long totalPlaytime = playtimeDataManager.getTotalPlaytime(uuid);
        int expectedRank = rankDataManager.getRankIndexForPlaytime(totalPlaytime);
        boolean promoted = expectedRank > currentRank;
        String track = rankDataManager.getTrackName();
        boolean isZenith = playtimeDataManager.hasZenith(uuid);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("PROMOTION_RESULT");
        out.writeUTF(uuid.toString());
        out.writeBoolean(promoted);
        out.writeInt(currentRank+1);
        out.writeUTF(track);
        out.writeBoolean(isZenith);

        ServerConnection serverConnection = (ServerConnection) event.getSource();
        serverConnection.sendPluginMessage(MinecraftChannelIdentifier.from("rankprogression:main"), out.toByteArray());
    }

    private void handleCheckZenithPromotion(ByteArrayDataInput in, PluginMessageEvent event) {

        UUID uuid = UUID.fromString(in.readUTF());

        if (playtimeDataManager.hasZenith(uuid)) {
            return;
        } else if (leaderboardManager.getCache().isEmpty()) {
            return;
        } else if (!uuid.equals(leaderboardManager.getCache().getFirst().getUuid())) {
            return;
        }

        UUID newZenithUUID = uuid;
        UUID oldZenithUUID = playtimeDataManager.getAllPlayers().stream().filter(playerUUID -> playtimeDataManager.hasZenith(playerUUID)).findFirst().orElse(null);

        if (oldZenithUUID == null) {
            oldZenithUUID = newZenithUUID;
        }

        String track = rankDataManager.getTrackName();
        int oldZenithsRankIndex = playtimeDataManager.getRankIndex(oldZenithUUID);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("ZENITH_PROMOTION_RESULT");
        out.writeUTF(newZenithUUID.toString());
        out.writeUTF(oldZenithUUID.toString());
        out.writeUTF(track);
        out.writeInt(oldZenithsRankIndex);

        ServerConnection serverConnection = (ServerConnection) event.getSource();
        serverConnection.sendPluginMessage(MinecraftChannelIdentifier.from("rankprogression:main"), out.toByteArray());
    }

    private void handlePlayerPromoted(ByteArrayDataInput in) {

        UUID uuid = UUID.fromString(in.readUTF());
        int rankIndex = in.readInt();

        playtimeDataManager.setRankIndex(uuid, rankIndex);
    }

    private void handlePlayerZenithPromoted(ByteArrayDataInput in) {

        UUID newZenithUUID = UUID.fromString(in.readUTF());
        UUID oldZenithUUID = UUID.fromString(in.readUTF());

        playtimeDataManager.setZenith(oldZenithUUID, false);
        playtimeDataManager.setZenith(newZenithUUID, true);
    }

    private void handleRankDataRequest(PluginMessageEvent event) {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("RANK_DATA_RESPONSE");

        out.writeInt(rankDataManager.getRanks().size());

        for (RankData rank : rankDataManager.getRanks()) {

            out.writeUTF(rank.getRankName());
            out.writeInt(rank.getIndex());
            out.writeLong(rank.getRequiredMinutes());
            out.writeUTF(rank.getRewardText());
            out.writeInt(rank.getRewardCommands().size()); for (String reward : rank.getRewardCommands()) {out.writeUTF(reward);}
            out.writeUTF(rank.getIcon());
            out.writeUTF(rank.getColor());
        }

        ServerConnection serverConnection = (ServerConnection) event.getSource();
        serverConnection.sendPluginMessage(CHANNEL, out.toByteArray());
    }

    private void handlePlayerStatsRequest(ByteArrayDataInput in, PluginMessageEvent event) {

        UUID uuid = UUID.fromString(in.readUTF());
        String reason = in.readUTF();

        long totalPlaytime = playtimeDataManager.getTotalPlaytime(uuid);
        int rankIndex = playtimeDataManager.getRankIndex(uuid);
        long firstJoin = playtimeDataManager.getFirstJoin(uuid);
        int joinCount = playtimeDataManager.getJoinCount(uuid);
        boolean isZenith = playtimeDataManager.hasZenith(uuid);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("PLAYER_STATS_RESPONSE");
        out.writeUTF(uuid.toString());
        out.writeLong(totalPlaytime);
        out.writeInt(rankIndex);
        out.writeLong(firstJoin);
        out.writeInt(joinCount);
        out.writeUTF(reason);
        out.writeBoolean(isZenith);

        ServerConnection connection = (ServerConnection) event.getSource();
        connection.sendPluginMessage(CHANNEL, out.toByteArray());
    }

    private void handleOfflineSyncPlayerData(ByteArrayDataInput in) {

        UUID uuid = UUID.fromString(in.readUTF());
        String username = in.readUTF();
        long playtimeMinutes = in.readLong();
        String serverName = in.readUTF();
        long firstJoin = in.readLong();
        int joinCount = in.readInt();
        long lastOnline = in.readLong();

        // Skip players that have already joined since migration
        if (playtimeDataManager.isPlayerInitialized(uuid)) {
            return;
        }

        playtimeDataManager.setUsername(uuid, username);
        playtimeDataManager.updateServerPlaytime(uuid, serverName, playtimeMinutes);
        playtimeDataManager.setFirstJoin(uuid, firstJoin);
        playtimeDataManager.setJoinCount(uuid, joinCount);
        playtimeDataManager.setLastSeen(uuid, lastOnline);

        int rankIndex = rankDataManager.getRankIndexForPlaytime(playtimeMinutes);

        playtimeDataManager.setRankIndex(uuid, rankIndex);
    }

    private void handlePlaytimeExportRequest(ByteArrayDataInput in, PluginMessageEvent event) {

        UUID requester = UUID.fromString(in.readUTF());

        CompletableFuture.runAsync(() -> {

            try {

                Path exportsFolder = playtimeDataManager.getDataFolder().resolve("exports");
                Files.createDirectories(exportsFolder);

                String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                Path exportFile = exportsFolder.resolve("playtime_export_" + timestamp + ".csv");

                Set<UUID> players = playtimeDataManager.getAllPlayers();

                try (FileWriter writer = new FileWriter(exportFile.toFile())) {

                    writer.write("uuid,name,total_minutes,total_hours,rank_index,first_join_timestamp,first_join_date,join_count,initialized\n");

                    for (UUID uuid : players) {

                        String name = playtimeDataManager.getUsername(uuid);
                        long totalMinutes = playtimeDataManager.getTotalPlaytime(uuid);
                        double totalHours = totalMinutes / 60.0;
                        int rankIndex = playtimeDataManager.getRankIndex(uuid);
                        long firstJoin = playtimeDataManager.getFirstJoin(uuid);
                        String firstJoinDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(firstJoin));
                        int joinCount = playtimeDataManager.getJoinCount(uuid);
                        boolean initialized = playtimeDataManager.isPlayerInitialized(uuid);

                        writer.write(uuid + "," +
                                        (name == null ? "" : name) + "," +
                                        totalMinutes + "," +
                                        String.format(Locale.US, "%.2f", totalHours) + "," +
                                        rankIndex + "," +
                                        firstJoin + "," +
                                        firstJoinDate + "," +
                                        joinCount + "," +
                                        initialized + "\n"
                        );
                    }
                }

                ByteArrayDataOutput out = ByteStreams.newDataOutput();

                out.writeUTF("PLAYTIME_EXPORT_COMPLETE");
                out.writeUTF(requester.toString());
                out.writeUTF(exportFile.getFileName().toString());
                out.writeInt(players.size());

                ServerConnection connection = (ServerConnection) event.getSource();
                connection.sendPluginMessage(CHANNEL, out.toByteArray());

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void handleLeaderboardRequest(ByteArrayDataInput in, PluginMessageEvent event) {

        UUID requester = UUID.fromString(in.readUTF());

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        ServerConnection connection = (ServerConnection) event.getSource();
        Player player = connection.getPlayer();

        out.writeUTF("LEADERBOARD_RESPONSE");
        out.writeUTF(requester.toString());

        // Viewer stats
        out.writeLong(playtimeDataManager.getTotalPlaytime(requester));
        out.writeInt(playtimeDataManager.getRankIndex(requester));
        out.writeLong(playtimeDataManager.getFirstJoin(requester));
        out.writeLong(playtimeDataManager.getLastSeen(requester));
        out.writeInt(playtimeDataManager.getJoinCount(requester));
        out.writeBoolean(playtimeDataManager.hasZenith(requester));
        out.writeBoolean(true);
        out.writeUTF(player.getCurrentServer().map(c -> c.getServerInfo().getName()).orElse("Unknown"));

        List<LeaderboardEntry> leaderboard = leaderboardManager.getCache();

        out.writeInt(leaderboard.size());

        for (LeaderboardEntry entry : leaderboard) {
            out.writeUTF(entry.getUuid().toString());
            out.writeUTF(entry.getUsername());
            out.writeLong(entry.getTotalMinutes());
            out.writeInt(entry.getRankIndex());
            out.writeLong(entry.getFirstJoin());
            out.writeLong(entry.getLastSeen());
            out.writeInt(entry.getJoinCount());
            out.writeBoolean(entry.getIsZenith());
            out.writeBoolean(entry.isOnline());
            out.writeUTF(entry.getServerName());
        }

        connection.sendPluginMessage(CHANNEL, out.toByteArray());
    }
}