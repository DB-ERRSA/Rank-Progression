package me.RedEagle3.rankProgression.Managers;

import me.RedEagle3.rankProgression.Models.RankMilestone;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerDataManager {

    private final JavaPlugin plugin;

    private File file;
    private FileConfiguration data;

    // CACHE: UUID -> rank index (0 = coal, 1 = iron, etc.)
    private final Map<UUID, Integer> rankCache = new HashMap<>();

    public PlayerDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadFile();
        loadCache();
    }

    private void loadFile() {
        file = new File(plugin.getDataFolder(), "playerdata.yml");

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        data = YamlConfiguration.loadConfiguration(file);
    }

    private void loadCache() {

        if (data.getConfigurationSection("players") == null) return;

        for (String key : data.getConfigurationSection("players").getKeys(false)) {

            UUID uuid = UUID.fromString(key);
            int rankIndex = data.getInt("players." + key + ".rankIndex", 0);

            rankCache.put(uuid, rankIndex);
        }
    }

    public int getRankIndex(UUID uuid) {
        return rankCache.getOrDefault(uuid, 0);
    }

    public void setRankIndex(UUID uuid, int index) {
        rankCache.put(uuid, index);
    }

    public void save() {

        for (Map.Entry<UUID, Integer> entry : rankCache.entrySet()) {
            data.set("players." + entry.getKey() + ".rankIndex", entry.getValue());
        }

        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void syncPlayer(UUID uuid, long playtimeMillis, List<RankMilestone> milestones) {

        int correctIndex = 0;

        for (RankMilestone milestone : milestones) {
            if (playtimeMillis >= milestone.getRequiredTime()) {
                correctIndex = milestone.getIndex();
            } else {
                break;
            }
        }

        int currentIndex = getRankIndex(uuid);

        if (correctIndex > currentIndex) {
            setRankIndex(uuid, correctIndex);
        }
    }

    public Set<UUID> getAllPlayers() {

        if (data.getConfigurationSection("players") == null) {
            return new HashSet<>();
        }

        return data.getConfigurationSection("players")
                .getKeys(false)
                .stream()
                .map(UUID::fromString)
                .collect(java.util.stream.Collectors.toSet());
    }

    private final Set<UUID> seen = new HashSet<>();

    public boolean hasSeenPlayer(UUID uuid) {
        return seen.contains(uuid);
    }

    public void markSeen(UUID uuid) {
        seen.add(uuid);
    }
}