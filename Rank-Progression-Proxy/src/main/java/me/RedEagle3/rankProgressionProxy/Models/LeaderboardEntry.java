package me.RedEagle3.rankProgressionProxy.Models;

import java.util.UUID;

public class LeaderboardEntry {

    private final UUID uuid;
    private final String username;

    private final long totalMinutes;
    private final int rankIndex;

    private final long firstJoin;
    private final long lastSeen;
    private final int joinCount;

    private final boolean online;
    private final String serverName;

    public LeaderboardEntry(
            UUID uuid,
            String username,
            long totalMinutes,
            int rankIndex,
            long firstJoin,
            long lastSeen,
            int joinCount,
            boolean online,
            String serverName
    ) {

        this.uuid = uuid;
        this.username = username;
        this.totalMinutes = totalMinutes;
        this.rankIndex = rankIndex;
        this.firstJoin = firstJoin;
        this.lastSeen = lastSeen;
        this.joinCount = joinCount;
        this.online = online;
        this.serverName = serverName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public long getTotalMinutes() {
        return totalMinutes;
    }

    public int getRankIndex() {
        return rankIndex;
    }

    public long getFirstJoin() {
        return firstJoin;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public int getJoinCount() {
        return joinCount;
    }

    public boolean isOnline() {
        return online;
    }

    public String getServerName() {
        return serverName;
    }
}