package org.skarvex.auth.velocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.skarvex.auth.core.service.AuthService;
import org.skarvex.auth.core.service.LoginTimeoutService;
import org.skarvex.auth.core.service.TimeService;
import org.skarvex.auth.velocity.manager.ConfigurationManager;
import org.skarvex.auth.velocity.utils.Messages;

import java.util.UUID;

public class LoginCommand implements SimpleCommand {

    private final ProxyServer server;
    private final ConfigurationManager config;
    private final AuthService authService;
    private final LoginTimeoutService loginTimeoutService;

    public LoginCommand(ProxyServer server,
                        ConfigurationManager config,
                        AuthService authService, LoginTimeoutService loginTimeoutService) {
        this.server = server;
        this.config = config;
        this.authService = authService;
        this.loginTimeoutService = loginTimeoutService;
    }

    @Override
    public void execute(Invocation invocation) {

        Player player = (Player) invocation.source();
        UUID uuid = player.getUniqueId();
        String ip = player.getRemoteAddress().getAddress().getHostAddress();

        String[] arguments = invocation.arguments();

        if (arguments.length != 1) {
            player.sendMessage(Messages.parse(config.getString
                    ("messages.login.usage"))
            );
            return;
        }

        if (authService.isAuthenticated(uuid)) {
            player.sendMessage(Messages.parse(
                    config.getString("messages.already-logged"))
            );
            return;
        }

        if (!authService.isRegistered(uuid)) {
            player.sendMessage(Messages.parse(config.getString("messages.not-registered")));
            return;
        }

        String password = arguments[0];

        if (password.length() < 4) {
            player.sendMessage(Messages.parse(
                    config.getString("messages.insufficient-characters")
            ));
            return;
        }

        if (!authService.authenticate(uuid, password, ip)) {

            authService.addAttempt(uuid);

            if (authService.getAttempts(uuid) >= authService.getMaxAttempts()) {
                authService.block(uuid);

                long time = authService.getRemainingTime(uuid);

                player.disconnect(Messages.parse(
                        config.getString("messages.login.kick-screen")
                                .replace("<time>", TimeService.formatDuration(time)))
                );
                return;
            }

            player.sendMessage(Messages.parse(
                    config.getString("messages.wrong-credentials")
            ));

            return;
        }

        authService.authenticate(uuid, password, ip);

        player.sendMessage(Messages.parse(
                config.getString("messages.login.success"))
        );

        authService.resetAttempts(uuid);
        loginTimeoutService.cancel(uuid);

        server.getServer("lobby").ifPresent(lobby ->
                player.createConnectionRequest(lobby).fireAndForget());
    }
}