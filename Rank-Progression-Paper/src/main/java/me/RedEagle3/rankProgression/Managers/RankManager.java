package me.RedEagle3.rankProgression.Managers;

import me.RedEagle3.rankProgression.Models.RankMilestone;
import me.RedEagle3.rankProgression.Utils.TimeParser;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class RankManager {

    private final JavaPlugin plugin;

    // Ordered list of milestones (VERY IMPORTANT)
    private final List<RankMilestone> milestones = new ArrayList<>();

    public RankManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadMilestones();
    }

    public void loadMilestones() {
        milestones.clear();

        FileConfiguration config = plugin.getConfig();

        // LinkedHashMap preserves YAML order
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("ranks");

        if (section == null) {
            plugin.getLogger().warning("No ranks section found in config.yml!");
            return;
        }

        int index = 0;

        for (String key : section.getKeys(false)) {

            String time = section.getString(key + ".requirement");
            String reward = section.getString(key + ".reward");
            String icon = section.getString(key + ".icon");
            String color = section.getString(key + ".color");

            long minutes = TimeParser.parseToMinutes(time);

            RankMilestone milestone = new RankMilestone(
                    key,
                    minutes,
                    reward,
                    index,
                    icon,
                    color
            );

            milestones.add(milestone);
            index++;
        }
    }

    public List<RankMilestone> getMilestones() {
        return milestones;
    }

    public int getRankIndexForPlaytime(long minutes) {

        int highest = -1;

        for (int i = 0; i < milestones.size(); i++) {

            if (minutes >= milestones.get(i).getRequiredTime()) {
                highest = i;
            }
        }

        return highest;
    }

    public RankMilestone getRank(int index) {

        if (index < 0 || index >= milestones.size()) {
            return null;
        }

        return milestones.get(index);
    }

    public RankMilestone getRankForPlaytime(long minutes) {

        int rankIndex = getRankIndexForPlaytime(minutes);

        return getRank(rankIndex);
    }

    public void assignRank(Player player, int rankIndex) {

        RankMilestone rank = getRank(rankIndex);

        if (rank == null || rankIndex < 0) {
            return;
        }

        // TODO: Pull other reward commands from config
        //String command = rank.getReward().replace("%player%", player.getName());
        String command = "lp user " + player.getName() + " parent add p-" + rank.getRankName();

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public void promoteRank(Player player, int rankIndex) {

        // TODO: Pull other reward commands from config
        //String command = rank.getReward().replace("%player%", player.getName());

        String track = plugin.getConfig().getString("track-name");
        String command = "lp user " + player.getName() + " promote " + track;

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}