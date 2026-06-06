package me.RedEagle3.rankProgressionProxy.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParser {

    // Matches things like: 1h, 30m, 2d, etc.
    private static final Pattern TIME_PATTERN =
            Pattern.compile("(\\d+)([smhd])");

    public static long parseToMinutes(String input) {

        if (input == null || input.isEmpty()) {
            return 0;
        }

        long totalMinutes = 0;

        Matcher matcher = TIME_PATTERN.matcher(input.toLowerCase());

        while (matcher.find()) {

            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {

                case "s":
                    totalMinutes += value / 60L;
                    break;

                case "m":
                    totalMinutes += value;
                    break;

                case "h":
                    totalMinutes += value * 60L;
                    break;

                case "d":
                    totalMinutes += value * 1440L;
                    break;
            }
        }

        return totalMinutes;
    }
}