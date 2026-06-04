package org.skarvex.auth.paper.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.skarvex.auth.core.service.SessionService;
import org.skarvex.auth.paper.AuthPaper;
import org.skarvex.auth.paper.manager.spawn.SpawnManager;
import org.skarvex.auth.paper.manager.title.TitleManager;
import org.skarvex.auth.paper.service.VisibilityService;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerListener implements Listener {

    private final SpawnManager spawnManager;
    private final VisibilityService visibilityService;

    private final SessionService sessions;
    private final AuthPaper plugin;
    private final TitleManager titleManager;

    public PlayerListener (SpawnManager spawnManager,
                           SessionService sessions,
                           VisibilityService visibilityService,
                           AuthPaper plugin,
                           TitleManager titleManager
    ) {
        this.spawnManager = spawnManager;
        this.visibilityService = visibilityService;
        this.sessions = sessions;
        this.plugin = plugin;
        this.titleManager = titleManager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.getLocation().getBlockY() <= 0.0F) {
            spawnManager.teleportToSpawn(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (sessions.isAuthenticated(uuid)) {
            return;
        }

        event.joinMessage(null);

        visibilityService.hideAllPlayers(player);
        spawnManager.teleportToSpawn(player);

        Bukkit.getScheduler().runTaskTimer(plugin, ()
                -> player.showTitle(titleManager.getTitle()),
                0L, 100L);

    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.quitMessage(null);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.deathMessage(null);
    }
}
