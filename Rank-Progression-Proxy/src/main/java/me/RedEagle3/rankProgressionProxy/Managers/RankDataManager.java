package me.RedEagle3.rankProgressionProxy.Managers;

import me.RedEagle3.rankProgressionProxy.Models.RankData;
import me.RedEagle3.rankProgressionProxy.Utils.TimeParser;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RankDataManager {

    private final Path file;
    private final YamlConfigurationLoader loader;
    private ConfigurationNode root;

    private String trackName;
    private final List<RankData> ranks = new ArrayList<>();

    public RankDataManager(Path dataFolder) throws IOException {

        Files.createDirectories(dataFolder);

        file = dataFolder.resolve("config.yml");
        loader = YamlConfigurationLoader.builder().path(file).build();

        if (Files.notExists(file)) {

            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {

                if (in == null) {
                    throw new IllegalStateException("Default config.yml not found in jar!");
                }

                Files.copy(in, file);
            }
        }

        root = loader.load();
        trackName = root.node("track-name").getString("rank_progression");
        loadRanks();
    }

    private void loadRanks() {

        ranks.clear();

        ConfigurationNode ranksNode = root.node("ranks");

        int index = 0;

        System.out.println("TEMP: Ranks node children: " + ranksNode.childrenMap().size());

        for (Map.Entry<Object, ? extends ConfigurationNode> entry :
                ranksNode.childrenMap().entrySet()) {

            String rankName = entry.getKey().toString();
            ConfigurationNode rankNode = entry.getValue();
            long requiredMinutes = TimeParser.parseToMinutes(rankNode.node("requirement").getString(""));

            List<String> rewards;
            try {rewards = rankNode.node("rewards").getList(String.class, List.of());}
            catch (SerializationException e) {throw new RuntimeException("Failed to load rewards for rank " + rankName, e);}

            String icon = rankNode.node("icon").getString("");
            String color = rankNode.node("color").getString("");

            ranks.add(new RankData(rankName, index, requiredMinutes, rewards, icon, color));

            index++;
        }
    }

    public RankData getRank(int index) {

        if (index < 0 || index >= ranks.size()) {
            return null;
        }

        return ranks.get(index);
    }

    public List<RankData> getRanks() {
        return ranks;
    }

    public int getRankIndexForPlaytime(long minutes) {

        int highest = -1;

        for (int i = 0; i < ranks.size(); i++) {

            if (minutes >= ranks.get(i).getRequiredMinutes()) {
                highest = i;
            }
        }

        return highest;
    }

    public String getTrackName() {
        return trackName;
    }
}
