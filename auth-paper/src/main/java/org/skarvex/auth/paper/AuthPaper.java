package org.skarvex.auth.paper;

import org.bukkit.plugin.java.JavaPlugin;
import org.skarvex.auth.core.service.SessionService;
import org.skarvex.auth.paper.listener.PlayerListener;
import org.skarvex.auth.paper.listener.SpawnListener;
import org.skarvex.auth.paper.listener.chat.ChatListener;
import org.skarvex.auth.paper.manager.config.ConfigurationManager;
import org.skarvex.auth.paper.manager.spawn.SpawnManager;
import org.skarvex.auth.paper.manager.title.TitleManager;
import org.skarvex.auth.paper.service.VisibilityService;
import org.skarvex.auth.paper.service.WorldService;

public final class AuthPaper extends JavaPlugin {

    private ConfigurationManager config;
    private SpawnManager spawnManager;
    private SessionService sessionService;
    private VisibilityService visibilityService;
    private WorldService worldService;
    private TitleManager titleManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        initServices();

        worldService.configureAll();

        getServer().getPluginManager().registerEvents(new PlayerListener(spawnManager, sessionService,
                visibilityService, this, titleManager),
                this);

        getServer().getPluginManager().registerEvents(new SpawnListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(sessionService, config), this);

        getLogger().info("SkarvexAuth has been successfully enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("SkarvexAuth has been successfully disabled.");
    }

    private void initServices() {
        worldService = new WorldService(this);
        config = new ConfigurationManager(this);
        spawnManager = new SpawnManager(config);
        sessionService = new SessionService();
        visibilityService = new VisibilityService(this);
        titleManager = new TitleManager(config);
    }
}
