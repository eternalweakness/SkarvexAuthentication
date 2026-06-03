package org.skarvex.auth.paper.listener.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.skarvex.auth.core.service.SessionService;
import org.skarvex.auth.paper.manager.config.ConfigurationManager;

import java.util.List;

public class ChatListener implements Listener {

    private final SessionService sessions;

    private final List<String> allowedCommands;

    public ChatListener(SessionService sessions, ConfigurationManager config) {
        this.sessions = sessions;

        String configPath = "settings.allowed-commands";
        this.allowedCommands = config.getList(configPath);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (sessions.isAuthenticated(player.getUniqueId())) return;

        String command = event.getMessage().toLowerCase();

        if (allowedCommands.contains(command.split(" ")[0])) {
            return;
        }

        event.setCancelled(true);
    }



}
