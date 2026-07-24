package me.RedEagle3.rankProgressionProxy.Managers;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlaytimeDataManager {

    private final Path file;
    private final YamlConfigurationLoader loader;
    private ConfigurationNode root;
    private final Path dataFolder;
    private ConfigurationNode playerNode(UUID uuid) {
        return root.node("players", uuid.toString());
    }

    public PlaytimeDataManager(Path dataFolder) throws IOException {

        this.dataFolder = dataFolder;

        Files.createDirectories(dataFolder);
        file = dataFolder.resolve("playtime-data.yml");

        loader = YamlConfigurationLoader.builder().path(file).build();

        if (Files.notExists(file)) {
            Files.createFile(file);
        }

        root = loader.load();

        save();
    }

    public Path getDataFolder() {
        return dataFolder;
    }

    public void load() throws IOException {
        root = loader.load();
    }

    public void save() throws IOException {
        loader.save(root);
    }

    public String getUsername(UUID uuid) {
        return playerNode(uuid).node("username").getString("Unknown");
    }

    public void setUsername(UUID uuid, String username) {

        safeSet(playerNode(uuid).node("username"), username);

        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save player data", e);
        }
    }

    public boolean isPlayerInitialized(UUID uuid) {
        return playerNode(uuid).node("initialized").getBoolean(false);
    }

    public void setPlayerInitialized(UUID uuid, boolean value) {
        try {
            playerNode(uuid).node("initialized").set(value);
            save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getRankIndex(UUID uuid) {
        return playerNode(uuid).node("rank-index").getInt(0);
    }

    public void setRankIndex(UUID uuid, int index) {
        try {playerNode(uuid).node("rank-index").set(index);
            save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getTotalPlaytime(UUID uuid) {
        return playerNode(uuid).node("total-playtime").getLong(0);
    }

    public long getServerPlaytime(UUID uuid, String server) {
        return playerNode(uuid).node("servers", server).getLong(0);
    }

    public void updateServerPlaytime(UUID uuid, String server, long minutes) {

        ConfigurationNode player = playerNode(uuid);

        // update per-server
        safeSet(player.node("servers", server), minutes);

        long total = 0;

        for (ConfigurationNode child : player.node("servers").childrenMap().values()) {
            total += child.getLong(0);
        }

        safeSet(player.node("total-playtime"), total);

        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save playtime data", e);
        }
    }

    public Set<UUID> getAllPlayers() {

        Set<UUID> players = new HashSet<>();

        ConfigurationNode playersNode = root.node("players");

        for (Object key : playersNode.childrenMap().keySet()) {

            try {
                players.add(UUID.fromString(key.toString()));
            } catch (IllegalArgumentException ignored) {}
        }

        return players;
    }

    public void setFirstJoin(UUID uuid, long firstJoin) {

        try {playerNode(uuid).node("first-join").set(firstJoin);
            save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getFirstJoin(UUID uuid) {
        return root.node("players", uuid.toString(), "first-join").getLong(0L);
    }

    public void setJoinCount(UUID uuid, int joinCount) {

        try {playerNode(uuid).node("join-count").set(joinCount);
            save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getJoinCount(UUID uuid) {
        return root.node("players", uuid.toString(), "join-count").getInt(0);
    }

    public boolean hasZenith(UUID uuid) {
        return playerNode(uuid).node("zenith").getBoolean(false);
    }

    public void setZenith(UUID uuid, boolean value) {
        try {playerNode(uuid).node("zenith").set(value);
            save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLastSeen(UUID uuid, long timestamp) {
        try {playerNode(uuid).node("last-seen").set(timestamp);
            save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getLastSeen(UUID uuid) {
        return playerNode(uuid).node("last-seen").getLong(0);
    }

    private void safeSet(ConfigurationNode node, Object value) {
        try {
            node.set(value);
        } catch (SerializationException e) {
            throw new RuntimeException("Failed to serialize config value: " + value, e);
        }
    }

    private ConfigurationNode playersNode() {
        return root.node("players");
    }
}
