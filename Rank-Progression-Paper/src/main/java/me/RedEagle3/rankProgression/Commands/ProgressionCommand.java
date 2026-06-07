package me.RedEagle3.rankProgression.Commands;

import me.RedEagle3.rankProgression.GUI.ProgressionGUI;
import me.RedEagle3.rankProgression.Messaging.ProxyMessenger;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ProgressionCommand implements CommandExecutor {

    private final ProgressionGUI gui;
    private final ProxyMessenger proxyMessenger;

    public ProgressionCommand(ProgressionGUI gui, ProxyMessenger proxyMessenger) {
        this.gui = gui;
        this.proxyMessenger = proxyMessenger;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            return true;
        }

        proxyMessenger.requestPlayerStats(player, "PROGRESSION_COMMAND");

        return true;
    }

    public void openProgression(Player player, long totalMinutes, int rankIndex) {
        gui.open(player, totalMinutes, rankIndex);
    }
}