package me.RedEagle3.rankProgression.Commands;

import me.RedEagle3.rankProgression.GUI.ProgressionGUI;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ProgressionCommand implements CommandExecutor {

    private final ProgressionGUI gui;

    public ProgressionCommand(ProgressionGUI gui) {
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        gui.open(player);
        return true;
    }
}