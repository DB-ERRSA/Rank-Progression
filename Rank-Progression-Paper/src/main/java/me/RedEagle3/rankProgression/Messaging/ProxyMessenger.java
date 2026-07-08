package me.RedEagle3.rankProgression.Messaging;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.RedEagle3.rankProgression.Managers.PlaytimeManager;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class ProxyMessenger {

    private static final String CHANNEL = "rankprogression:main";

    private final JavaPlugin plugin;
    private  final PlaytimeManager playtimeManager;

    public ProxyMessenger(JavaPlugin plugin, PlaytimeManager playtimeManager) {
        this.plugin = plugin;
        this.playtimeManager = playtimeManager;
    }

    public void updatePlaytime(Player player) {

        long minutes = playtimeManager.getLocalPlaytimeMinutes(player);

        // TODO: Change this to get server name from velocity instead of local config
        String serverName = plugin.getConfig().getString("server-name");
        if (serverName == null || serverName.isBlank()) {serverName = "unknown";}

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("UPDATE_PLAYTIME");
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(serverName);
        out.writeLong(minutes);

        player.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
    }

    public void playerJoin(Player player) {

        long minutes = playtimeManager.getLocalPlaytimeMinutes(player);

        String serverName = plugin.getConfig().getString("server-name");
        if (serverName == null || serverName.isBlank()) {serverName = "unknown";}

        long firstPlayed = player.getFirstPlayed();
        int joinCount = player.getStatistic(Statistic.LEAVE_GAME) + 1;

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("PLAYER_JOIN");
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(player.getName());
        out.writeUTF(serverName);
        out.writeLong(minutes);
        out.writeLong(firstPlayed);
        out.writeInt(joinCount);

        player.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
    }

    public void playerInitialized(Player player, int rankIndex) {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("PLAYER_INITIALIZED");
        out.writeUTF(player.getUniqueId().toString());
        out.writeInt(rankIndex);

        player.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
    }

    public void checkPromotion(Player player) {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("CHECK_PROMOTION");
        out.writeUTF(player.getUniqueId().toString());

        player.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
    }

    public void playerPromoted(Player player, int rankIndex) {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("PLAYER_PROMOTED");
        out.writeUTF(player.getUniqueId().toString());
        out.writeInt(rankIndex);

        player.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
    }

    public void requestRankData(Player player) {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("REQUEST_RANK_DATA");

        player.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
    }

    public void requestPlayerStats(Player player, String reason) {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("REQUEST_PLAYER_STATS");
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(reason);

        player.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
    }

    public void syncOfflinePlayer(Player admin, UUID uuid, String username, long playtimeMinutes, String serverName, long firstJoin, int joinCount, long lastOnline) {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("OFFLINE_SYNC_PLAYER_DATA");
        out.writeUTF(uuid.toString());
        out.writeUTF(username == null ? "Unknown" : username);
        out.writeLong(playtimeMinutes);
        out.writeUTF(serverName);
        out.writeLong(firstJoin);
        out.writeInt(joinCount);
        out.writeLong(lastOnline);

        admin.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
    }

    public void requestPlaytimeExport(Player admin) {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("REQUEST_PLAYTIME_EXPORT");
        out.writeUTF(admin.getUniqueId().toString());

        admin.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
    }

    public void requestLeaderboard(Player player) {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("REQUEST_LEADERBOARD");
        out.writeUTF(player.getUniqueId().toString());

        player.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
    }
}