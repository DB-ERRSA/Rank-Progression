package me.RedEagle3.rankProgression.Utils;

public class TimeFormatter {

    public static String format(long millis) {

        long totalMinutes = millis / 60000;

        long days = totalMinutes / 1440;
        long hours = (totalMinutes % 1440) / 60;
        long minutes = totalMinutes % 60;

        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days).append("d ");
        }

        if (hours > 0) {
            sb.append(hours).append("h ");
        }

        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }

        // fallback so we never return empty
        if (sb.length() == 0) {
            return "0m";
        }

        return sb.toString().trim();
    }
}