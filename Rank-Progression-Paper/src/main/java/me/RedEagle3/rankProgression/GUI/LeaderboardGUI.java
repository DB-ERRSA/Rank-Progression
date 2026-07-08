package me.RedEagle3.rankProgression.GUI;

import me.RedEagle3.rankProgression.Managers.LeaderboardCacheManager;
import me.RedEagle3.rankProgression.Managers.RankManager;
import me.RedEagle3.rankProgression.Models.LeaderboardEntry;
import me.RedEagle3.rankProgression.Utils.TextFormatter;
import me.RedEagle3.rankProgression.Utils.TimeFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LeaderboardGUI {

    private final LeaderboardCacheManager leaderboardCacheManager;
    private final RankManager rankManager;

    public LeaderboardGUI(
            LeaderboardCacheManager leaderboardCacheManager,
            RankManager rankManager
    ) {
        this.leaderboardCacheManager = leaderboardCacheManager;
        this.rankManager = rankManager;
    }


    public void open(Player viewer, LeaderboardEntry viewerData) {

        Inventory inv = Bukkit.createInventory(null, 54, "§6Playtime Leaderboard");

        List<LeaderboardEntry> top = leaderboardCacheManager.getLeaderboard();

        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };


        for (int i = 0; i < top.size() && i < slots.length; i++) {

            LeaderboardEntry entry = top.get(i);

            ItemStack head = createPlayerHead(entry, i);

            inv.setItem(slots[i], head);
        }

        // Bottom middle slot = viewer stats
        inv.setItem(49, createSelfIcon(viewer, viewerData));

        viewer.openInventory(inv);
    }


    private ItemStack createPlayerHead(LeaderboardEntry entry, int i) {

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getUuid());
        meta.setOwningPlayer(player);

        meta.setDisplayName("§6#" + (i + 1) + " §7- §b" + entry.getUsername());


        String rankDisplay = "§7Unknown";
        int rankIndex = entry.getRankIndex();
        if (rankIndex >= 0 && rankIndex < rankManager.getMilestones().size()) {
            rankDisplay = TextFormatter.getRankPrintLine(rankManager, rankIndex);
        }

        String status;

        if (entry.isOnline()) {
            String rawServerName = entry.getServerName();
            String serverName = rawServerName.substring(0, 1).toUpperCase() + rawServerName.substring(1);
            status = "§7Status: §aOnline - " + serverName;
        } else {
            long ago = System.currentTimeMillis() - entry.getLastSeen();
            status = "§7Last Seen: §f" + TimeFormatter.format(ago/60000);
        }

        meta.setLore(List.of(
                "§7Playtime: §f" + TimeFormatter.format(entry.getTotalMinutes()),
                "§7Rank: " + rankDisplay,
                "§7First Joined: §f" + new SimpleDateFormat("MMM dd yyyy").format(new Date(entry.getFirstJoin())),
                "§7Times Joined: §f" + entry.getJoinCount(),
                status
        ));

        head.setItemMeta(meta);

        return head;
    }

    private ItemStack createSelfIcon(Player viewer, LeaderboardEntry viewerData) {

        ItemStack self = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) self.getItemMeta();

        meta.setOwningPlayer(viewer);

        meta.setDisplayName("§aYour Stats");

        String rankDisplay = "Unknown";

        int rankIndex = viewerData.getRankIndex();

        if (rankIndex >= 0 && rankIndex < rankManager.getMilestones().size()) {
            rankDisplay = TextFormatter.getRankPrintLine(rankManager, rankIndex);
        }

        String status;

        if (viewerData.isOnline()) {
            String rawServerName = viewerData.getServerName();
            String serverName = rawServerName.substring(0, 1).toUpperCase() + rawServerName.substring(1);
            status = "§7Status: §aOnline - " + serverName;
        } else {
            long ago = System.currentTimeMillis() - viewerData.getLastSeen();
            status = "§7Last Seen: §f" + TimeFormatter.format(ago);
        }

        String firstJoin = "Unknown";

        if (viewerData.getFirstJoin() > 0) {
            firstJoin = new SimpleDateFormat("MMM dd yyyy")
                    .format(new Date(viewerData.getFirstJoin()));
        }

        meta.setLore(List.of(
                "§7Playtime: §f" + TimeFormatter.format(viewerData.getTotalMinutes()),
                "§7Rank: " + rankDisplay,
                "§7First Joined: §f" + firstJoin,
                "§7Times Joined: §f" + viewerData.getJoinCount(),
                status
        ));

        self.setItemMeta(meta);

        return self;
    }
}