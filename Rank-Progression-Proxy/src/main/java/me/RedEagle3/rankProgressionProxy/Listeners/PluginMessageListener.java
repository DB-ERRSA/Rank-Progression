package me.RedEagle3.rankProgressionProxy.Listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;


public class PluginMessageListener {

    private static final MinecraftChannelIdentifier CHANNEL = MinecraftChannelIdentifier.from("rankprogression:main");

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {

        if (!event.getIdentifier().getId().equals("rankprogression:main")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());

        String msg = in.readUTF();

        System.out.println("Message: " + msg);
    }
}