package org.skarvex.auth.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.skarvex.auth.core.repository.RestrictedIpRepository;
import org.skarvex.auth.core.repository.UserRepository;
import org.skarvex.auth.core.security.BCryptPasswordHasher;
import org.skarvex.auth.core.security.PasswordHasher;
import org.skarvex.auth.core.service.AuthService;
import org.skarvex.auth.core.service.BruteForceService;
import org.skarvex.auth.core.service.LoginTimeoutService;
import org.skarvex.auth.core.service.SessionService;
import org.skarvex.auth.core.util.TimeUtil;
import org.skarvex.auth.velocity.commands.LoginCommand;
import org.skarvex.auth.velocity.commands.LogoutCommand;
import org.skarvex.auth.velocity.commands.RegisterCommand;
import org.skarvex.auth.velocity.commands.ResetPasswordCommand;
import org.skarvex.auth.velocity.database.DatabaseManager;
import org.skarvex.auth.velocity.database.JdbcRestrictedIpRepository;
import org.skarvex.auth.velocity.database.JdbcUserRepository;
import org.skarvex.auth.velocity.manager.ConfigurationManager;
import org.skarvex.auth.velocity.scheduler.LoginReminderScheduler;
import org.skarvex.auth.velocity.scheduler.LoginTimeoutScheduler;
import org.skarvex.auth.velocity.utils.Messages;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

public class AuthVelocity {

    @Inject private Logger logger;
    @Inject private ProxyServer proxy;

    @Inject
    @DataDirectory
    private Path dataDirectory;

    private ConfigurationManager config;

    private SessionService sessions;
    private PasswordHasher hasher;
    private UserRepository users;
    private RestrictedIpRepository restrictedIps;
    private DatabaseManager database;
    private AuthService authService;

    private LoginTimeoutService loginTimeoutService;
    private BruteForceService bruteForceService;

    private LoginTimeoutScheduler timeoutScheduler;
    private LoginReminderScheduler reminderScheduler;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        loadConfiguration();
        setupDatabase();
        setupServices();
        registerCommands();
        registerSchedulers();
    }

    @Subscribe
    public void onConnect(ServerPreConnectEvent event) {

        Player customer = event.getPlayer();

        if (authService.isAuthenticated(customer.getUniqueId())) {
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

        Player customer = event.getPlayer();
        UUID uuid = customer.getUniqueId();
        String incomingIp = customer.getRemoteAddress().getAddress().getHostAddress();

        boolean autoLogin = authService.tryAutoLogin(uuid, incomingIp);

        if (autoLogin) {
            proxy.getServer("lobby").ifPresent(event::setInitialServer);
            return;
        }

        bruteForceService.isBlocked(incomingIp).thenAccept(isBlocked -> {
            if (!isBlocked) return;

            Duration time = bruteForceService.getRemainingTime(incomingIp);
            customer.disconnect(
                    Messages.parse(config.getString("messages.login.kick-screen")
                            .replace("<time>", TimeUtil.formatDuration(time))
                    )
            );
        });

        // Running the timeout service (default = 60 seconds)
        loginTimeoutService.start(uuid, Duration.ofSeconds(config.getInt("login.timeout")));
        timeoutScheduler.startTimeout(uuid);

        if (authService.isAuthenticated(uuid)) {
            return;
        }

        reminderScheduler.start(uuid);

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

    @Subscribe
    public void onDisconnect(
            DisconnectEvent event
    ) {
        Player customer = event.getPlayer();
        UUID uuid = customer.getUniqueId();

        authService.logout(uuid);

        loginTimeoutService.cancel(uuid);
        timeoutScheduler.removeTask(uuid);
    }

    private void loadConfiguration() {
        this.config = new ConfigurationManager(dataDirectory);
        config.load();
    }

    private void setupDatabase() {
        this.database = new DatabaseManager(
                config,
                logger
        );

        database.connect();
    }

    private void setupServices() {
        this.sessions = new SessionService();
        this.hasher = new BCryptPasswordHasher();
        this.users = new JdbcUserRepository(database);
        this.restrictedIps = new JdbcRestrictedIpRepository(database);
        this.loginTimeoutService = new LoginTimeoutService();
        this.authService = new AuthService(users, hasher, sessions);
        this.bruteForceService = new BruteForceService(restrictedIps);
    }

    private void registerCommands() {
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
                        authService,
                        bruteForceService,
                        loginTimeoutService
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

    private void registerSchedulers() {
        this.timeoutScheduler = new LoginTimeoutScheduler(this, proxy, authService, loginTimeoutService, config);
        this.reminderScheduler = new LoginReminderScheduler(this, proxy, authService, config);
    }
}
