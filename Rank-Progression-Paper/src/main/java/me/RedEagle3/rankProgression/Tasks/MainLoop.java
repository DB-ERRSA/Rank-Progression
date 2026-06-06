package me.RedEagle3.rankProgression.Tasks;

import me.RedEagle3.rankProgression.Messaging.ProxyMessenger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MainLoop extends BukkitRunnable {

    private final ProxyMessenger proxyMessenger;

    public MainLoop(ProxyMessenger proxyMessenger) {
        this.proxyMessenger = proxyMessenger;
    }

    @Override
    public void run() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            proxyMessenger.updatePlaytime(player);
            proxyMessenger.checkPromotion(player);
            //proxyMessenger.checkZenith(player); TODO: Implement
        }
    }
}
