package me.RedEagle3.rankProgressionProxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import me.RedEagle3.rankProgressionProxy.Listeners.PluginMessageListener;
import me.RedEagle3.rankProgressionProxy.Managers.PlaytimeDataManager;
import me.RedEagle3.rankProgressionProxy.Managers.RankDataManager;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.UUID;

@Plugin(
        id = "rankprogressionproxy",
        name = "RankProgressionProxy",
        version = "1.0"
)

public class RankProgressionProxy {

    private final ProxyServer server;
    private final Logger logger;

    private static final MinecraftChannelIdentifier CHANNEL = MinecraftChannelIdentifier.from("rankprogression:main");
    private PlaytimeDataManager playtimeDataManager;
    private RankDataManager rankDataManager;

    @Inject
    public RankProgressionProxy(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

        logger.info("Registering channel: {}", CHANNEL.getId());

        server.getChannelRegistrar().register(CHANNEL);

        logger.info("Channel registered!");

        try {Path dataFolder = Path.of("plugins", "RankProgressionProxy");
            playtimeDataManager = new PlaytimeDataManager(dataFolder);
            rankDataManager = new RankDataManager(dataFolder);
        }
        catch (Exception e) {
            logger.error("Failed to initialize RankProgressionProxy", e);
        }

        server.getEventManager().register(this, new PluginMessageListener(this, playtimeDataManager, rankDataManager));

        logger.info("RankProgressionProxy enabled!");
    }

    @Subscribe
    public void onLogin(PostLoginEvent event) {

        UUID uuid = event.getPlayer().getUniqueId();

        if (!playtimeDataManager.isPlayerInitialized(uuid)) {return;}

        int newJoinCount = playtimeDataManager.getJoinCount(uuid) + 1;
        playtimeDataManager.setJoinCount(uuid, newJoinCount);

        logger.info("TEMP: " + event.getPlayer().getUsername() + " joined, incrementing join-count to: " + newJoinCount);
    }
}