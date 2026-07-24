package me.RedEagle3.rankProgression.Managers;

import me.RedEagle3.rankProgression.Models.RankMilestone;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class RankManager {

    private final JavaPlugin plugin;
    private boolean loaded;
    private final Set<UUID> waitingForRankData = new HashSet<>();

    // Ordered list of ranks
    private final List<RankMilestone> milestones = new ArrayList<>();

    public RankManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public List<RankMilestone> getMilestones() {
        return milestones;
    }

    public RankMilestone getRank(int index) {

        if (index < 0 || index >= milestones.size()) {
            return null;
        }

        return milestones.get(index);
    }

    public void assignRank(Player player, int rankIndex) {

        RankMilestone rank = getRank(rankIndex);

        if (rank == null || rankIndex < 0) {
            return;
        }

        for (int i = 0; i <= rankIndex; i++) {

            RankMilestone milestone = getRank(i);

            for (String command : milestone.getRewardCommands()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
            }
        }

        String command = "lp user " + player.getName() + " parent add p-" + rank.getRankName();

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public void promoteRank(Player player, int rankIndex, String track) {

        RankMilestone milestone = getRank(rankIndex);

        for (String command : milestone.getRewardCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
        }

        String command = "lp user " + player.getName() + " promote " + track;

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public void zenithPromoteRank(Player player, int rankIndex, String track) {

        RankMilestone milestone = getRank(rankIndex);

        for (String command : milestone.getRewardCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
        }
    }

    public void setZenithDemote(OfflinePlayer oldZenithPlayer, int oldZenithsRankIndex, String track) {

        RankMilestone rank = getRank(oldZenithsRankIndex);
        if (rank == null) {return;}

        String command = "lp user " + oldZenithPlayer.getName() + " parent settrack " + track + " p-" + rank.getRankName();

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public void setZenithPromote(Player newZenithPlayer, String track) {

        String command = "lp user " + newZenithPlayer.getName() + " parent settrack " + track + " p-zenith";

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public void clearRanks() {
        milestones.clear();
    }

    public void addRank(RankMilestone milestone) {
        milestones.add(milestone);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public Set<UUID> getWaitingForRankData() {
        return waitingForRankData;
    }
}