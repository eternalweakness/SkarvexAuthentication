package org.skarvex.auth.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.skarvex.auth.core.repository.UserRepository;
import org.skarvex.auth.core.security.BCryptPasswordHasher;
import org.skarvex.auth.core.security.PasswordHasher;
import org.skarvex.auth.core.service.AuthService;
import org.skarvex.auth.core.service.SessionService;
import org.skarvex.auth.velocity.commands.LoginCommand;
import org.skarvex.auth.velocity.commands.LogoutCommand;
import org.skarvex.auth.velocity.commands.RegisterCommand;
import org.skarvex.auth.velocity.commands.ResetPasswordCommand;
import org.skarvex.auth.velocity.database.DatabaseManager;
import org.skarvex.auth.velocity.database.JdbcUserRepository;
import org.skarvex.auth.velocity.manager.ConfigurationManager;
import org.skarvex.auth.velocity.utils.Messages;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.time.Duration;

public class AuthVelocity {

    @Inject private Logger logger;
    @Inject private ProxyServer proxy;

    @Inject
    @DataDirectory
    private Path dataDirectory;

    private ConfigurationManager config;

    private DatabaseManager database;
    private AuthService authService;


    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.config = new ConfigurationManager(dataDirectory);
        config.load();

        this.database = new DatabaseManager(
                config,
                logger
        );

        database.connect();

        SessionService sessions =
                new SessionService();

        PasswordHasher hasher =
                new BCryptPasswordHasher();

        UserRepository users =
                new JdbcUserRepository(database);

        authService = new AuthService(
                users,
                hasher,
                sessions
        );

        proxy.getCommandManager().register(
                proxy.getCommandManager()
                        .metaBuilder("register")
                        .aliases("reg")
                        .build(),
                new RegisterCommand(
                        proxy,
                        config,
                        authService
                )
        );

        proxy.getCommandManager().register(
                proxy.getCommandManager()
                        .metaBuilder("login")
                        .aliases("l")
                        .build(),
                new LoginCommand(
                        proxy,
                        config,
                        authService
                )
        );

        proxy.getCommandManager().register(
                proxy.getCommandManager()
                        .metaBuilder("logout")
                        .build(),
                new LogoutCommand(
                        proxy,
                        config,
                        authService
                )
        );

        proxy.getCommandManager().register(
                proxy.getCommandManager()
                        .metaBuilder("changepass")
                        .build(),
                new ResetPasswordCommand(
                        proxy,
                        config,
                        authService
                )
        );

    }

    @Subscribe
    public void onConnect(ServerPreConnectEvent event) {

        Player player = event.getPlayer();

        if (authService.isAuthenticated(player.getUniqueId())) {
            return;
        }

        String target =
                event.getOriginalServer()
                        .getServerInfo()
                        .getName();

        if (target.equals("auth")) {
            return;
        }

        event.setResult(
                ServerPreConnectEvent.ServerResult.denied()
        );
    }

    @Subscribe
    public void onJoin(PlayerChooseInitialServerEvent event) {

        Player player = event.getPlayer();

        if (authService.isAuthenticated(player.getUniqueId())) {
            return;
        }

        proxy.getScheduler()
                .buildTask(this, () -> {
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

        proxy.getServer("auth")
                .ifPresent(event::setInitialServer);
    }

    @Subscribe
    public void onProxyShutdown(
            ProxyShutdownEvent event
    ) {
        if (database != null) {
            database.disconnect();
        }
    }
}
