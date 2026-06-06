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

import java.util.UUID;


public class PluginMessageListener {

    private static final MinecraftChannelIdentifier CHANNEL = MinecraftChannelIdentifier.from("rankprogression:main");
    private final PlaytimeDataManager playtimeDataManager;
    private final RankDataManager rankDataManager;

    public PluginMessageListener(PlaytimeDataManager playtimeDataManager, RankDataManager rankDataManager) {
        this.playtimeDataManager = playtimeDataManager;
        this.rankDataManager = rankDataManager;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {

        System.out.println("TEMP: PLUGIN MESSAGE RECEIVED");
        System.out.println("TEMP: Channel: " + event.getIdentifier().getId());

        if (!event.getIdentifier().equals(CHANNEL)) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());

        String subChannel = in.readUTF();

        System.out.println("TEMP: SUBCHANNEL = " + subChannel);

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

        ServerConnection connection = (ServerConnection) event.getSource();
        connection.sendPluginMessage(CHANNEL, out.toByteArray());
    }
}