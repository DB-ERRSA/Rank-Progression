package me.RedEagle3.rankProgression.GUI;

import me.RedEagle3.rankProgression.Managers.PlayerDataManager;
import me.RedEagle3.rankProgression.Managers.PlaytimeManager;
import me.RedEagle3.rankProgression.Managers.RankManager;
import me.RedEagle3.rankProgression.Models.RankMilestone;
import me.RedEagle3.rankProgression.Utils.TimeFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ProgressionGUI {

    private final JavaPlugin plugin;
    private final RankManager rankManager;
    private final PlayerDataManager playerDataManager;
    private final PlaytimeManager playtimeManager;

    public ProgressionGUI(JavaPlugin plugin,
                          RankManager rankManager,
                          PlayerDataManager playerDataManager,
                          PlaytimeManager playtimeManager) {

        this.plugin = plugin;
        this.rankManager = rankManager;
        this.playerDataManager = playerDataManager;
        this.playtimeManager = playtimeManager;
    }

    public void open(Player player) {

        Inventory inv = Bukkit.createInventory(null, 36, "§6Rank Progression");

        List<RankMilestone> milestones = rankManager.getMilestones();

        int currentIndex = playerDataManager.getRankIndex(player.getUniqueId());

        // === MAIN RANK ITEMS ===
        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25
        };

        for (int i = 0; i < milestones.size() && i < slots.length; i++) {

            RankMilestone milestone = milestones.get(i);

            long required = milestone.getRequiredMillis();
            long current = playtimeManager.getPlaytimeMillis(player.getUniqueId());

            boolean achieved = current >= required;

            Material mat = getMaterialForRank(
                    milestone.getRankName(),
                    achieved
            );

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();

            List<String> lore = new ArrayList<>();

            String rankColor = color(milestone.getColor());
            String icon = milestone.getIcon();
            String reward = milestone.getReward();

            int progress = getProgress(current, required);

            // TITLE
            if (achieved) {
                meta.setDisplayName("§aAchieved! - " +
                        "§7[" + rankColor + capitalize(milestone.getRankName()) + "§7] §aRank");
            } else {
                meta.setDisplayName(
                        "§7[" + rankColor + capitalize(milestone.getRankName()) + "§7] §fRank"
                );
            }

            // LORE
            if (achieved) {
                lore.add("§7Earned for reaching §f" + TimeFormatter.format(required) + "§7!");
            } else {
                lore.add("§7Earn by reaching §f" + TimeFormatter.format(required) + ".");
            }
            lore.add("");
            lore.add("§fProgress: §b" + progress + "%");
            lore.add("");
            lore.add("§fRank icon: §7[" + rankColor + icon + "§7]");
            lore.add("§fReward: §7" + reward);
            lore.add("");

            if (achieved) {
                lore.add("§aReward already claimed!");
            } else {
                lore.add("§7Not yet eligible");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);

            inv.setItem(slots[i], item);
        }

        // === ZENITH SLOT ===
        if (currentIndex >= milestones.size() - 1) {

            ItemStack zenith = new ItemStack(Material.WITHER_SKELETON_SKULL);
            ItemMeta meta = zenith.getItemMeta();

            meta.setDisplayName("§5§lZENITH");

            meta.setLore(List.of(
                    "§7Ultimate Rank",
                    "§7Status: §6Unlocked",
                    "§7You are top of the progression!"
            ));

            zenith.setItemMeta(meta);

            inv.setItem(31, zenith);
        }

        player.openInventory(inv);
    }

    private Material getMaterialForRank(String rank, boolean achieved) {

        return switch (rank.toLowerCase()) {

            case "coal" -> achieved ? Material.COAL_BLOCK : Material.COAL;
            case "iron" -> achieved ? Material.IRON_BLOCK : Material.IRON_INGOT;
            case "quartz" -> achieved ? Material.QUARTZ_BLOCK : Material.QUARTZ;
            case "copper" -> achieved ? Material.COPPER_BLOCK : Material.COPPER_INGOT;
            case "gold" -> achieved ? Material.GOLD_BLOCK : Material.GOLD_INGOT;
            case "lapis" -> achieved ? Material.LAPIS_BLOCK : Material.LAPIS_LAZULI;
            case "redstone" -> achieved ? Material.REDSTONE_BLOCK : Material.REDSTONE;
            case "amethyst" -> achieved ? Material.AMETHYST_BLOCK : Material.AMETHYST_CLUSTER;
            case "emerald" -> achieved ? Material.EMERALD_BLOCK : Material.EMERALD;
            case "diamond" -> achieved ? Material.DIAMOND_BLOCK : Material.DIAMOND;
            case "netherite" -> achieved ? Material.NETHERITE_BLOCK : Material.NETHERITE_INGOT;
            case "echo" -> achieved ? Material.SCULK : Material.ECHO_SHARD;
            case "ender" -> achieved ? Material.END_STONE : Material.ENDER_EYE;
            case "astral" -> achieved ? Material.BEACON : Material.NETHER_STAR;

            default -> Material.BARRIER;
        };
    }

    private int getProgress(long current, long required) {

        if (required <= 0) return 100;

        double percent = (double) current / required * 100;

        return Math.min(100, (int) percent);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String color(String text) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
    }
}