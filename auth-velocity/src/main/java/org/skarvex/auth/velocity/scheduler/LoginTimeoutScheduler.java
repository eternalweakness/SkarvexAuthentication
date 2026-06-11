package org.skarvex.auth.velocity.scheduler;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import org.skarvex.auth.core.service.AuthService;
import org.skarvex.auth.core.service.LoginTimeoutService;
import org.skarvex.auth.velocity.AuthVelocity;
import org.skarvex.auth.velocity.manager.ConfigurationManager;
import org.skarvex.auth.velocity.utils.Messages;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LoginTimeoutScheduler {

    private final AuthVelocity plugin;
    private final ProxyServer proxy;
    private final AuthService authService;
    private final LoginTimeoutService loginTimeoutService;
    private final ConfigurationManager config;

    private final Map<UUID, ScheduledTask> reminderTasks = new ConcurrentHashMap<>();

    public LoginTimeoutScheduler(AuthVelocity plugin,
                                 ProxyServer proxy,
                                 AuthService authService,
                                 LoginTimeoutService loginTimeoutService,
                                 ConfigurationManager config) {
        this.plugin = plugin;
        this.proxy = proxy;
        this.authService = authService;
        this.loginTimeoutService = loginTimeoutService;
        this.config = config;
    }

    public void startTimeout(UUID uuid) {

        removeTask(uuid);

        ScheduledTask task = proxy.getScheduler().buildTask(plugin, () -> {
            var player = proxy.getPlayer(uuid).orElse(null);

            if (player == null) {
                removeTask(uuid);
                return;
            }

            if (authService.isAuthenticated(uuid)) {
                removeTask(uuid);
                return;
            }

            if (loginTimeoutService.isExpired(uuid)) {
                player.disconnect(Messages.parse(
                        config.getString("messages.timeout")
                ));
                removeTask(uuid);
            }

        }).repeat(Duration.ofSeconds(1)).schedule();

        reminderTasks.put(uuid, task);
    }

    public void removeTask(UUID uuid) {
        ScheduledTask task = reminderTasks.remove(uuid);
        if (task != null) task.cancel();
    }
}
