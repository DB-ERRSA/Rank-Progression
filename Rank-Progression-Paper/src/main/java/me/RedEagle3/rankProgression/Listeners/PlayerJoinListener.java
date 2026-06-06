package me.RedEagle3.rankProgression.Listeners;

import me.RedEagle3.rankProgression.Messaging.ProxyMessenger;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerJoinListener implements Listener {

    private final JavaPlugin plugin;
    private final ProxyMessenger proxyMessenger;
    private static final long INITIAL_SYNC_DELAY = 40L;

    public PlayerJoinListener(JavaPlugin plugin, ProxyMessenger proxyMessenger) {
        this.plugin = plugin;
        this.proxyMessenger = proxyMessenger;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Bukkit.getScheduler().runTaskLater(plugin, () -> proxyMessenger.playerJoin(event.getPlayer()), INITIAL_SYNC_DELAY);
    }
}
