package me.RedEagle3.rankProgression.Managers;

import me.RedEagle3.rankProgression.Models.LeaderboardEntry;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardCacheManager {

    private List<LeaderboardEntry> leaderboard = new ArrayList<>();

    public void updateLeaderboard(List<LeaderboardEntry> entries) {
        this.leaderboard = new ArrayList<>(entries);
    }

    public List<LeaderboardEntry> getLeaderboard() {
        return leaderboard;
    }

    public void clear() {
        leaderboard.clear();
    }
}
