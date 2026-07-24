package me.RedEagle3.rankProgression.GUI;

import me.RedEagle3.rankProgression.Managers.LeaderboardCacheManager;
import me.RedEagle3.rankProgression.Managers.RankManager;
import me.RedEagle3.rankProgression.Models.LeaderboardEntry;
import me.RedEagle3.rankProgression.Utils.TextFormatter;
import me.RedEagle3.rankProgression.Utils.TimeFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.profile.PlayerProfile;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LeaderboardGUI {

    private final LeaderboardCacheManager leaderboardCacheManager;
    private final RankManager rankManager;
    private  final Plugin plugin;

    private final Map<UUID, PlayerProfile> profileCache = new ConcurrentHashMap<>();
    private final Set<UUID> loadingProfiles = ConcurrentHashMap.newKeySet();

    private static final String TITLE = "§6Playtime Leaderboard";

    public LeaderboardGUI(LeaderboardCacheManager leaderboardCacheManager, RankManager rankManager, Plugin plugin) {
        this.leaderboardCacheManager = leaderboardCacheManager;
        this.rankManager = rankManager;
        this.plugin = plugin;
    }

    public void open(Player viewer, LeaderboardEntry viewerData) {

        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        List<LeaderboardEntry> top = leaderboardCacheManager.getLeaderboard();

        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        boolean needsRefresh = false;

        for (int i = 0; i < top.size() && i < slots.length; i++) {

            LeaderboardEntry entry = top.get(i);

            if (!profileCache.containsKey(entry.getUuid())) {
                startProfileLoad(entry);
                needsRefresh = true;
            }

            inv.setItem(slots[i], createPlayerHead(entry, i));
        }

        inv.setItem(49, createSelfIcon(viewer, viewerData));

        viewer.openInventory(inv);

        if (needsRefresh) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!viewer.isOnline()) {return;}
                if (!viewer.getOpenInventory().getTitle().equals(TITLE)) {return;}
                open(viewer, viewerData);
            }, 30L);
        }
    }

    private void startProfileLoad(LeaderboardEntry entry) {

        UUID uuid = entry.getUuid();

        if (!loadingProfiles.add(uuid)) {return;}

        PlayerProfile profile = Bukkit.createPlayerProfile(uuid, entry.getUsername());

        profile.update().thenAccept(updated -> {
            loadingProfiles.remove(uuid);
            if (updated == null) {return;}
            profileCache.put(uuid, updated);
        });
    }

    private ItemStack createPlayerHead(LeaderboardEntry entry, int i) {

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        PlayerProfile profile = profileCache.get(entry.getUuid());

        if (profile != null) {
            meta.setOwnerProfile(profile);
        }

        meta.setDisplayName("§6#" + (i + 1) + " §7- §b" + entry.getUsername());

        String rankDisplay = "§7Unknown";

        int rankIndex = 0;
        if (entry.getIsZenith()) {
            rankIndex = rankManager.getMilestones().size()-1;
        } else {
            rankIndex = entry.getRankIndex();
        }

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

        int rankIndex = 0;
        if (viewerData.getIsZenith()) {
            rankIndex = rankManager.getMilestones().size()-1;
        } else {
            rankIndex = viewerData.getRankIndex();
        }

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