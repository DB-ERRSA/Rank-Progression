package me.RedEagle3.rankProgression.Commands;

import me.RedEagle3.rankProgression.Managers.PlayerDataManager;
import me.RedEagle3.rankProgression.Managers.PlaytimeManager;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class SavePlaytimeDataCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final PlaytimeManager playtimeManager;
    private final PlayerDataManager playerDataManager;

    public SavePlaytimeDataCommand(JavaPlugin plugin,
                                   PlaytimeManager playtimeManager,
                                   PlayerDataManager playerDataManager) {

        this.plugin = plugin;
        this.playtimeManager = playtimeManager;
        this.playerDataManager = playerDataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("rankprogression.admin")) {
            sender.sendMessage("§cNo permission.");
            return true;
        }

        sender.sendMessage("§eExporting playtime data...");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            try {
                exportData(sender);
            } catch (Exception e) {
                sender.sendMessage("§cFailed to export data. Check console.");
                e.printStackTrace();
            }
        });

        return true;
    }

    private void exportData(CommandSender sender) throws Exception {

        File folder = new File(plugin.getDataFolder(), "exports");
        if (!folder.exists()) folder.mkdirs();

        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        File file = new File(folder, "playtime_export_" + timestamp + ".csv");

        FileWriter writer = new FileWriter(file);

        writer.write("uuid,name,playtime_hours,playtime_minutes\n");

        for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {

            UUID uuid = op.getUniqueId();

            long ms = playtimeManager.getPlaytimeMillis(uuid);

            double hours = ms / 3600000.0;
            long minutes = ms / 60000;

            String name = op.getName();
            if (name == null) name = uuid.toString();

            writer.write(uuid + "," +
                    name + "," +
                    String.format("%.2f", hours) + "," +
                    minutes + "\n");
        }

        writer.flush();
        writer.close();

        Bukkit.getScheduler().runTask(plugin, () -> {
            sender.sendMessage("§aExport complete!");
            sender.sendMessage("§7Saved to: §f" + file.getPath());
        });
    }
}