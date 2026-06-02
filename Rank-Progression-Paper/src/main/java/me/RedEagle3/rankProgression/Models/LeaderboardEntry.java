package me.RedEagle3.rankProgression.Models;

import java.util.UUID;

public class LeaderboardEntry {

    private final UUID uuid;
    private final long playtime;

    public LeaderboardEntry(UUID uuid, long playtime) {
        this.uuid = uuid;
        this.playtime = playtime;
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getPlaytime() {
        return playtime;
    }
}