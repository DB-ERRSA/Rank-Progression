package me.RedEagle3.rankProgression.Commands;

import me.RedEagle3.rankProgression.Messaging.ProxyMessenger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ExportPlaytimeDataCommand implements CommandExecutor {

    private final ProxyMessenger proxyMessenger;

    public ExportPlaytimeDataCommand(ProxyMessenger proxyMessenger) {
        this.proxyMessenger = proxyMessenger;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player admin)) {
            sender.sendMessage("This command can only be used in-game.");
            return true;
        }

        if (!admin.hasPermission("rankprogression.exportplaytimedata")) {
            admin.sendMessage("§cYou do not have permission.");
            return true;
        }

        admin.sendMessage("§eGenerating export...");

        proxyMessenger.requestPlaytimeExport(admin);

        return true;
    }
}