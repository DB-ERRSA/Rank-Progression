package me.RedEagle3.rankProgression.Commands;

import me.RedEagle3.rankProgression.GUI.LeaderboardGUI;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class PlaytimeTopCommand implements CommandExecutor { // TODO: Not needed?

    private final LeaderboardGUI gui;

    public PlaytimeTopCommand(LeaderboardGUI gui) {
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            return true;
        }

        //gui.open(player);
        return true;
    }
}