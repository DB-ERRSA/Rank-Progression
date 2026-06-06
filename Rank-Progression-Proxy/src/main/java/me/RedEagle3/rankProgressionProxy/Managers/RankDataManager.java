package me.RedEagle3.rankProgressionProxy.Managers;

import java.util.ArrayList;
import java.util.List;

public class RankDataManager {

    private final List<Long> rankRequirements = new ArrayList<>();

    public RankDataManager() {

        // TODO: Load from config (on velocity eventually)
        // temporary values
        rankRequirements.add(60L);      // Coal
        rankRequirements.add(180L);     // Iron
        rankRequirements.add(300L);     // Quartz
        rankRequirements.add(720L);     // Copper
        rankRequirements.add(1440L);
        rankRequirements.add(2880L);
        rankRequirements.add(5760L);
        rankRequirements.add(9000L);
        rankRequirements.add(15000L);
        rankRequirements.add(24000L);
        rankRequirements.add(39000L);
        rankRequirements.add(60000L);
        rankRequirements.add(90000L);
        rankRequirements.add(90000L);
        rankRequirements.add(150000L);
        rankRequirements.add(600000L);
    }

    public int getRankIndexForPlaytime(long minutes) {

        int highest = -1;

        for (int i = 0; i < rankRequirements.size(); i++) {

            if (minutes >= rankRequirements.get(i)) {
                highest = i;
            }
        }

        return highest;
    }
}
