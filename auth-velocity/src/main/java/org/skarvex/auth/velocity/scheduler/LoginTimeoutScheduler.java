package org.skarvex.auth.velocity.manager;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import org.skarvex.auth.core.service.AuthService;
import org.skarvex.auth.core.service.LoginTimeoutService;
import org.skarvex.auth.velocity.utils.Messages;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SchedulerManager {

    private final ProxyServer proxy;
    private final AuthService authService;
    private final LoginTimeoutService loginTimeoutService;
    private final ConfigurationManager config;

    private Map<UUID, ScheduledTask> reminderTasks = new ConcurrentHashMap<>();

    public SchedulerManager(ProxyServer proxy, AuthService authService, LoginTimeoutService loginTimeoutService, ConfigurationManager config) {
        this.proxy = proxy;
        this.authService = authService;
        this.loginTimeoutService = loginTimeoutService;
        this.config = config;
    }

    public ScheduledTask scheduleTask(UUID uuid) {
        return proxy.getScheduler().buildTask(this, () -> proxy.getAllPlayers()
                .forEach(player -> {
                    if (authService.isAuthenticated(uuid)) return;
                    if (loginTimeoutService.isExpired(uuid)) {
                        player.disconnect(Messages.parse(config.getString("messages.timeout")));
                    }
                })).repeat(Duration.ofSeconds(1)).schedule();
    }

    public void removeTask(UUID uuid) {
        reminderTasks.remove(uuid);
    }
}
