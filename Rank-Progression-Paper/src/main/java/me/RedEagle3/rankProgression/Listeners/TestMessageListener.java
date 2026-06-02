package me.RedEagle3.rankProgression.Listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class TestMessageListener implements Listener {

    private final JavaPlugin plugin;

    public TestMessageListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("HELLO_FROM_PAPER");

        player.sendPluginMessage(plugin, "rankprogression:main", out.toByteArray());

        plugin.getLogger().info("Sent test message to proxy!");
    }
}