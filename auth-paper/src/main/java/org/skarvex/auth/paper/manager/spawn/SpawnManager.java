package org.skarvex.auth.paper.manager.spawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.skarvex.auth.paper.manager.config.ConfigurationManager;

public class SpawnManager {
    private final ConfigurationManager config;

    private final String spawn_base = "spawn.location";
    private final String world = "spawn.world";

    public SpawnManager(ConfigurationManager config) {
        this.config = config;
    }

    public void teleportToSpawn(Player player) {
        player.teleport(getSpawnLocation());
    }

    public Location getSpawnLocation() {
        return new Location(
                getWorld(),
                config.getDouble(spawn_base + ".x"),
                config.getDouble(spawn_base + ".y"),
                config.getDouble(spawn_base + ".z"),
                (float) config.getDouble(spawn_base + ".yaw"),
                (float) config.getDouble(spawn_base + ".pitch")
        );
    }

    public World getWorld() {
        return Bukkit.getWorld(config.getString(world));
    }
}
