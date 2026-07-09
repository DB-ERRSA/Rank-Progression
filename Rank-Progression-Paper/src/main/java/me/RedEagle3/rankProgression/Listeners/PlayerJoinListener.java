package me.RedEagle3.rankProgression.Listeners;

import me.RedEagle3.rankProgression.Managers.RankManager;
import me.RedEagle3.rankProgression.Messaging.ProxyMessenger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerJoinListener implements Listener {

    private final JavaPlugin plugin;
    private final ProxyMessenger proxyMessenger;
    private  final RankManager rankManager;
    private static final long INITIAL_SYNC_DELAY = 40L;

    public PlayerJoinListener(JavaPlugin plugin, ProxyMessenger proxyMessenger, RankManager rankManager) {
        this.plugin = plugin;
        this.proxyMessenger = proxyMessenger;
        this.rankManager = rankManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        if (!rankManager.isLoaded()) {
            rankManager.getWaitingForRankData().add(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin, () -> proxyMessenger.requestRankData(player), INITIAL_SYNC_DELAY/2);
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> proxyMessenger.playerJoin(event.getPlayer()), INITIAL_SYNC_DELAY);
    }
}
