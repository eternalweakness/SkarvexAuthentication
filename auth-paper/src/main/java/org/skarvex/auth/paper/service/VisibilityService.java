package org.skarvex.auth.paper.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.skarvex.auth.paper.AuthPaper;

public class VisibilityService {

    private final AuthPaper plugin;

    public VisibilityService(AuthPaper plugin) {
        this.plugin = plugin;
    }

    public void hideAllPlayers(Player player) {
        Bukkit.getOnlinePlayers().forEach(online -> {

            if (online.equals(player)) {
                return;
            }

            player.hidePlayer(plugin, online);
            online.hidePlayer(plugin, player);
        });
    }

}
