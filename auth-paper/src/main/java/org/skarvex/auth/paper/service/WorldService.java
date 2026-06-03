package org.skarvex.auth.paper.service;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.skarvex.auth.paper.AuthPaper;

public final class WorldService {

    private final AuthPaper plugin;

    public WorldService(AuthPaper plugin) {
        this.plugin = plugin;
    }

    public void configureAll() {
        Bukkit.getWorlds().forEach(this::configure);
        Bukkit.getScheduler().runTaskTimer(
                plugin,
                () -> Bukkit.getWorlds()
                        .forEach(this::clearEntities), 20L, 1L);
    }

    public void configure(World world) {

        // World rules
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_MOB_LOOT, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.FIRE_DAMAGE, false);
        world.setGameRule(GameRule.FALL_DAMAGE, false);
        world.setGameRule(GameRule.MOB_GRIEFING, false);
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
    }

    public void clearEntities(World world) {
        world.getLivingEntities().stream()
               .filter(entity ->
                       !(entity instanceof Player))
               .forEach(Entity::remove);
    }
}
