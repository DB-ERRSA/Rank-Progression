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

        // always update username
        playtimeDataManager.setUsername(uuid, username);

        playtimeDataManager.updateServerPlaytime(uuid, server, minutes);

        // fetch state
        boolean initialized = playtimeDataManager.isPlayerInitialized(uuid);
        long totalPlaytime = playtimeDataManager.getTotalPlaytime(uuid);

        // build response
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("PLAYER_INIT_STATUS");
        out.writeUTF(uuid.toString());
        out.writeBoolean(initialized);
        out.writeLong(totalPlaytime);

        ServerConnection serverConnection = (ServerConnection) event.getSource();
        serverConnection.sendPluginMessage(MinecraftChannelIdentifier.from("rankprogression:main"), out.toByteArray());
    }

    private void handlePlayerInitialized(ByteArrayDataInput in) {

        UUID uuid = UUID.fromString(in.readUTF());
        int rankIndex = in.readInt();

        playtimeDataManager.setPlayerInitialized(uuid, true);
        playtimeDataManager.setRankIndex(uuid, rankIndex);

        System.out.println("Initialized " + uuid + " at rank " + rankIndex);
    }

    private void handleCheckPromotion(ByteArrayDataInput in, PluginMessageEvent event) {

        UUID uuid = UUID.fromString(in.readUTF());
        int currentRank = playtimeDataManager.getRankIndex(uuid);
        long totalPlaytime = playtimeDataManager.getTotalPlaytime(uuid);

        int expectedRank = rankDataManager.getRankIndexForPlaytime(totalPlaytime);

        boolean promoted = expectedRank > currentRank;

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("PROMOTION_RESULT");
        out.writeUTF(uuid.toString());
        out.writeBoolean(promoted);
        out.writeInt(expectedRank);

        ServerConnection serverConnection = (ServerConnection) event.getSource();
        serverConnection.sendPluginMessage(MinecraftChannelIdentifier.from("rankprogression:main"), out.toByteArray());
    }

    private void handlePlayerPromoted(ByteArrayDataInput in) {

        UUID uuid = UUID.fromString(in.readUTF());
        int rankIndex = in.readInt();

        playtimeDataManager.setRankIndex(uuid, rankIndex);
    }
}