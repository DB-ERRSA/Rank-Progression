package me.RedEagle3.rankProgression.Managers;

import me.RedEagle3.rankProgression.Models.RankMilestone;
import me.RedEagle3.rankProgression.Utils.TimeParser;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.ConfigurationSection;

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

            long millis = TimeParser.parseToMillis(time);

            RankMilestone milestone = new RankMilestone(
                    key,
                    millis,
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
}