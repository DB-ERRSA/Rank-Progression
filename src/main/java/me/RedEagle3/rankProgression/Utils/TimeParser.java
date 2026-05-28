package me.RedEagle3.rankProgression.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParser {

    // Matches things like: 1h, 30m, 2d, etc.
    private static final Pattern TIME_PATTERN =
            Pattern.compile("(\\d+)([smhd])");

    public static long parseToMillis(String input) {
        if (input == null || input.isEmpty()) {
            return 0;
        }

        long totalMillis = 0;

        Matcher matcher = TIME_PATTERN.matcher(input.toLowerCase());

        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {
                case "s":
                    totalMillis += value * 1000L;
                    break;
                case "m":
                    totalMillis += value * 60_000L;
                    break;
                case "h":
                    totalMillis += value * 3_600_000L;
                    break;
                case "d":
                    totalMillis += value * 86_400_000L;
                    break;
            }
        }

        return totalMillis;
    }
}