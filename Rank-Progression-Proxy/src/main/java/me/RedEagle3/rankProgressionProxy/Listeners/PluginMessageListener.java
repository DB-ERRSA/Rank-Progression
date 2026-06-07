package me.RedEagle3.rankProgressionProxy.Listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import me.RedEagle3.rankProgressionProxy.Managers.PlaytimeDataManager;
import me.RedEagle3.rankProgressionProxy.Managers.RankDataManager;
import me.RedEagle3.rankProgressionProxy.Models.RankData;
import me.RedEagle3.rankProgressionProxy.RankProgressionProxy;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public class PluginMessageListener {

    private final RankProgressionProxy plugin;
    private static final MinecraftChannelIdentifier CHANNEL = MinecraftChannelIdentifier.from("rankprogression:main");
    private final PlaytimeDataManager playtimeDataManager;
    private final RankDataManager rankDataManager;

    public PluginMessageListener(RankProgressionProxy plugin, PlaytimeDataManager playtimeDataManager, RankDataManager rankDataManager) {
        this.plugin = plugin;
        this.playtimeDataManager = playtimeDataManager;
        this.rankDataManager = rankDataManager;
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

            case "PLAYER_PROMOTED":
                handlePlayerPromoted(in);
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

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("PROMOTION_RESULT");
        out.writeUTF(uuid.toString());
        out.writeBoolean(promoted);
        out.writeInt(currentRank+1); // TODO: Changed this from expectedRank to currentRank+1 to handle if you're behind a rank, not tested though
        out.writeUTF(track);

        ServerConnection serverConnection = (ServerConnection) event.getSource();
        serverConnection.sendPluginMessage(MinecraftChannelIdentifier.from("rankprogression:main"), out.toByteArray());
    }

    private void handlePlayerPromoted(ByteArrayDataInput in) {

        UUID uuid = UUID.fromString(in.readUTF());
        int rankIndex = in.readInt();

        playtimeDataManager.setRankIndex(uuid, rankIndex);
    }

    private void handleRankDataRequest(PluginMessageEvent event) {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("RANK_DATA_RESPONSE");

        out.writeInt(rankDataManager.getRanks().size());

        for (RankData rank : rankDataManager.getRanks()) {

            out.writeUTF(rank.getRankName());
            out.writeInt(rank.getIndex());
            out.writeLong(rank.getRequiredMinutes());
            out.writeInt(rank.getRewards().size()); for (String reward : rank.getRewards()) {out.writeUTF(reward);}
            out.writeUTF(rank.getIcon());
            out.writeUTF(rank.getColor());
        }

        System.out.println("TEMP: Sending " + rankDataManager.getRanks().size() + " ranks");

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

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("PLAYER_STATS_RESPONSE");
        out.writeUTF(uuid.toString());
        out.writeLong(totalPlaytime);
        out.writeInt(rankIndex);
        out.writeLong(firstJoin);
        out.writeInt(joinCount);
        out.writeUTF(reason);

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

        // Skip players that have already joined since migration
        if (playtimeDataManager.isPlayerInitialized(uuid)) {
            return;
        }

        playtimeDataManager.setUsername(uuid, username);
        playtimeDataManager.updateServerPlaytime(uuid, serverName, playtimeMinutes);
        playtimeDataManager.setFirstJoin(uuid, firstJoin);
        playtimeDataManager.setJoinCount(uuid, joinCount);

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
}