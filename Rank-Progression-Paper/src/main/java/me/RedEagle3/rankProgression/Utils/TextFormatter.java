package me.RedEagle3.rankProgression.Utils;

import me.RedEagle3.rankProgression.Managers.RankManager;

public class TextFormatter {

    public static String capitalize(String s) {

        if (s == null || s.isEmpty()) return s;

        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static String color(String text) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String getRankPrintLine(RankManager rankManager, int rankIndex) {
        String color = TextFormatter.color(rankManager.getRank(rankIndex).getColor());
        String name = TextFormatter.capitalize(rankManager.getRank(rankIndex).getRankName());
        return "§7[" + color + name + "§7]";
    }
}
