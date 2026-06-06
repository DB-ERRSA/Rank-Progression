package me.RedEagle3.rankProgression.Messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.RedEagle3.rankProgression.Managers.PlayerInitializationManager;
import me.RedEagle3.rankProgression.Managers.RankManager;
import me.RedEagle3.rankProgression.Utils.TextFormatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.UUID;

public class ProxyMessageListener implements PluginMessageListener {

    private final JavaPlugin plugin;
    private final ProxyMessenger proxyMessenger;
    private final PlayerInitializationManager initializationManager;
    private final RankManager rankManager;

    public ProxyMessageListener(JavaPlugin plugin, ProxyMessenger proxyMessenger, PlayerInitializationManager initializationManager, RankManager rankManager) {
        this.plugin = plugin;
        this.proxyMessenger = proxyMessenger;
        this.initializationManager = initializationManager;
        this.rankManager = rankManager;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {

        if (!channel.equals("rankprogression:main")) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);

        String subChannel = in.readUTF();

        switch (subChannel) {

            case "PLAYER_INIT_STATUS":
                handleInitStatus(in);
                break;

            case "PROMOTION_RESULT":
                handlePromotionResult(in);
                break;

            default:
                plugin.getLogger().info("Unknown subchannel: " + subChannel);
                break;
        }
    }

    private void handleInitStatus(ByteArrayDataInput in) {

        UUID uuid = UUID.fromString(in.readUTF());
        boolean initialized = in.readBoolean();
        long totalPlaytime = in.readLong();

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        if (!initialized) {
            initializationManager.initializePlayer(player, totalPlaytime);
        } else {
            plugin.getLogger().info(player.getName() + " is already initialized, updating playtime.");
            proxyMessenger.updatePlaytime(player);
            // initializationManager.handleReturningPlayer(player);
        }
    }

    private void handlePromotionResult(ByteArrayDataInput in) {

        UUID uuid = UUID.fromString(in.readUTF());
        boolean promoted = in.readBoolean();
        int rankIndex = in.readInt();

        if (!promoted) {return;}

        Player player = Bukkit.getPlayer(uuid);

        if (player == null) {return;}

        rankManager.promoteRank(player, rankIndex);

        // TODO: Enable/disable global broadcast
        String rankLine = TextFormatter.getRankPrintLine(rankManager, rankIndex);
        Bukkit.broadcastMessage("§b"+ player.getName() + " §6has achieved " + rankLine + " §6rank!");

        proxyMessenger.playerPromoted(player, rankIndex);
    }
}