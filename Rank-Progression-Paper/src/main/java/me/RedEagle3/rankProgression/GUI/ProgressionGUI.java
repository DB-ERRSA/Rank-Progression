package me.RedEagle3.rankProgression.GUI;

import me.RedEagle3.rankProgression.Managers.RankManager;
import me.RedEagle3.rankProgression.Models.RankMilestone;
import me.RedEagle3.rankProgression.Utils.TextFormatter;
import me.RedEagle3.rankProgression.Utils.TimeFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ProgressionGUI {

    private final RankManager rankManager;

    public ProgressionGUI(RankManager rankManager) {
        this.rankManager = rankManager;
    }

    public void open(Player player, long currentMinutesPT, int currentRankIndex, boolean isZenith) {

        Inventory inv = Bukkit.createInventory(null, 36, "§6Rank Progression");

        List<RankMilestone> milestones = rankManager.getMilestones();

        // === MAIN RANK ITEMS ===
        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25
        };

        for (int i = 0; i < milestones.size() && i < slots.length; i++) {

            RankMilestone milestone = milestones.get(i);

            long required = milestone.getRequiredMinutes();
            boolean achieved = currentMinutesPT >= required;

            Material mat = getMaterialForRank(milestone.getRankName(), achieved);

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();

            String rankLine = "§7Unknown";
            if (milestone.getIndex() >= 0 && milestone.getIndex() < rankManager.getMilestones().size()) {
                rankLine = TextFormatter.getRankPrintLine(rankManager, milestone.getIndex());
            }

            String icon = milestone.getIcon();
            String rewardText = milestone.getRewardText();
            int progress = getProgress(currentMinutesPT, required);

            // TITLE
            if (achieved) {
                meta.setDisplayName("§aAchieved! - " + rankLine + " §aRank");
            } else {
                meta.setDisplayName(rankLine + " §fRank");
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
            lore.add("§fRank icon: §7[" + TextFormatter.color(rankManager.getRank(milestone.getIndex()).getColor()) + icon + "§7]");
            if (!rewardText.isEmpty()) {lore.add("§fReward: §7" + rewardText);}
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
        if (isZenith) {

            ItemStack zenith = new ItemStack(Material.WITHER_SKELETON_SKULL);
            ItemMeta meta = zenith.getItemMeta();

            meta.setDisplayName("§6Achieved! - §8[§4Zenith§8] §6Rank");

            meta.setLore(List.of(
                    "§cEarned by having the top playtime!",
                    "",
                    "§cStatus: §6Unlocked!",
                    "",
                    "§cRank icon: §8[§4☠§8]",
                    "",
                    "§6You are top of the progression!"
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
}