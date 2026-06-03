package org.skarvex.auth.paper.listener.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;

public class ChatManager {

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        event.setCancelled(true);
    }



}
