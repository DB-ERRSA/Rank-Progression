package me.RedEagle3.rankProgression.Commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ProxyTestCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public ProxyTestCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender,
                             Command command,
                             String label,
                             String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("HELLO_FROM_PAPER");

        player.sendPluginMessage(
                plugin,
                "rankprogression:main",
                out.toByteArray()
        );

        player.sendMessage("§aSent plugin message!");

        plugin.getLogger().info("Test plugin message sent.");

        return true;
    }
}