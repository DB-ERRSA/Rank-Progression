package me.RedEagle3.rankProgressionProxy.Managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class PlaytimeDataManager {

    private final File file;
    private final YamlConfiguration config;

    public PlaytimeDataManager(Path dataDirectory) {

        file = dataDirectory.resolve("playtime-data.yml").toFile();

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public YamlConfiguration getConfig() {
        return config;
    }
}
