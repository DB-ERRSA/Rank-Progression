package me.RedEagle3.rankProgression.GUI;

import me.RedEagle3.rankProgression.Managers.LeaderboardManager;
import me.RedEagle3.rankProgression.Managers.PlayerDataManager;
import me.RedEagle3.rankProgression.Managers.PlaytimeManager;
import me.RedEagle3.rankProgression.Managers.RankManager;
import me.RedEagle3.rankProgression.Models.LeaderboardEntry;
import me.RedEagle3.rankProgression.Models.RankMilestone;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import me.RedEagle3.rankProgression.Utils.TimeFormatter;
import org.bukkit.Statistic;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LeaderboardGUI {

    private final LeaderboardManager leaderboardManager;
    private final PlaytimeManager playtimeManager;
    private final RankManager rankManager;
    private  final PlayerDataManager playerDataManager;

    public LeaderboardGUI(LeaderboardManager leaderboardManager,
                          PlaytimeManager playtimeManager, RankManager rankManager, PlayerDataManager playerDataManager) {
        this.leaderboardManager = leaderboardManager;
        this.playtimeManager = playtimeManager;
        this.rankManager = rankManager;
        this.playerDataManager = playerDataManager;
    }

    public void open(Player viewer) {

        Inventory inv = Bukkit.createInventory(null, 54, "§6Playtime Leaderboard");

        var top = leaderboardManager.getTop();

        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        // Fill leaderboard slots
        for (int i = 0; i < top.size() && i < slots.length; i++) {

            LeaderboardEntry entry = top.get(i);
            OfflinePlayer target = Bukkit.getOfflinePlayer(entry.getUuid());

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();

            meta.setOwningPlayer(target);

            long ms = entry.getPlaytime();

            meta.setDisplayName("§6#" + (i + 1) + " §7- §b" + target.getName());

            long firstPlayed = target.getFirstPlayed();

            String firstJoin = "Unknown";

            if (firstPlayed > 0) {
                firstJoin = new SimpleDateFormat("MMM dd yyyy")
                        .format(new Date(firstPlayed));
            }

            int joins = 0;

            if (target.isOnline()) {
                joins = target.getPlayer().getStatistic(Statistic.LEAVE_GAME) + 1;
            }

            String rankDisplay = "Unknown";

            int rankIndex = playerDataManager.getRankIndex(target.getUniqueId());

            if (rankIndex >= 0 && rankIndex < rankManager.getMilestones().size()) {

                RankMilestone milestone =
                        rankManager.getMilestones().get(rankIndex);

                String color = color(milestone.getColor());

                String rankName = capitalize(milestone.getRankName());

                rankDisplay = "§7[" + color + rankName + "§7]";
            }

            String lastSeenLine;

            if (target.isOnline()) {
                lastSeenLine = "§7Status: §aOnline";
            } else {
                long lastPlayed = target.getLastPlayed();
                long timeAgo = System.currentTimeMillis() - lastPlayed;
                lastSeenLine = "§7Last Seen: §f" + TimeFormatter.format(timeAgo);
            }

            meta.setLore(java.util.Arrays.asList(
                    "§7Playtime: §f" + TimeFormatter.format(ms),
                    "§7Rank: " + rankDisplay,
                    "§7First Joined: §f" + firstJoin,
                    "§7Times Joined: §f" + joins,
                    lastSeenLine
            ));

            head.setItemMeta(meta);

            inv.setItem(slots[i], head);
        }

        // Bottom middle slot = viewer stats
        int slot = 49;

        ItemStack self = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) self.getItemMeta();

        meta.setOwningPlayer(viewer);

        long selfMs = playtimeManager.getPlaytimeMillis(viewer.getUniqueId());
        long selfHours = selfMs / 3_600_000;

        meta.setDisplayName("§aYour Stats");

        meta.setLore(java.util.Arrays.asList(
                "§7Playtime: §f" + TimeFormatter.format(selfMs),
                "§7First Join: §f" + new SimpleDateFormat("MMM dd yyyy")
                        .format(new Date(viewer.getFirstPlayed()))
        ));

        self.setItemMeta(meta);

        inv.setItem(slot, self);

        viewer.openInventory(inv);
    }

    private String capitalize(String s) {

        if (s == null || s.isEmpty()) return s;

        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String color(String text) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
    }
}