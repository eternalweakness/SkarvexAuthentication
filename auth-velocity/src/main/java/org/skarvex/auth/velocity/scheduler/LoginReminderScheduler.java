package org.skarvex.auth.velocity.scheduler;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import org.skarvex.auth.core.service.AuthService;
import org.skarvex.auth.velocity.AuthVelocity;
import org.skarvex.auth.velocity.manager.ConfigurationManager;
import org.skarvex.auth.velocity.utils.Messages;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LoginReminderScheduler {

    private final AuthVelocity plugin;
    private final ProxyServer proxy;
    private final AuthService authService;
    private final ConfigurationManager config;

    private final Map<UUID, ScheduledTask> tasks = new ConcurrentHashMap<>();

    public LoginReminderScheduler(AuthVelocity plugin, ProxyServer proxy, AuthService authService, ConfigurationManager config) {
        this.plugin = plugin;
        this.proxy = proxy;
        this.authService = authService;
        this.config = config;
    }

    public void start(UUID uuid) {

        stop(uuid);

        ScheduledTask task = proxy.getScheduler()
                .buildTask(plugin, () -> {

                    Player player  = proxy.getPlayer(uuid).orElse(null);

                    if (player == null) {
                        stop(uuid);
                        return;
                    }

                    if (!authService.isRegistered(player.getUniqueId())) {
                        player.sendMessage(Messages.parse(
                                config.getString("messages.register.usage"))
                        );
                        return;
                    }

                    if (!authService.isAuthenticated(player.getUniqueId())) {
                        player.sendMessage(Messages.parse(
                                config.getString("messages.login.usage"))
                        );
                    }

                })
                .repeat(Duration.ofSeconds(3))
                .schedule();

        tasks.put(uuid, task);
    }

    public void stop(UUID uuid) {
        ScheduledTask task = tasks.remove(uuid);

        if (task != null) task.cancel();
    }
}
