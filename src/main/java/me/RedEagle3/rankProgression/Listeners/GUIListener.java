package me.RedEagle3.rankProgression.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.Event;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (event.getView().getTitle().equals("§6Playtime Leaderboard") ||
                event.getView().getTitle().equals("§6Rank Progression")) {

            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }
    }
}