package org.skarvex.auth.velocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.skarvex.auth.core.service.AuthService;
import org.skarvex.auth.velocity.manager.ConfigurationManager;
import org.skarvex.auth.velocity.utils.Messages;

import java.util.UUID;

public class LoginCommand implements SimpleCommand {

    private final ProxyServer server;
    private final ConfigurationManager config;
    private final AuthService authService;

    public LoginCommand(ProxyServer server,
                        ConfigurationManager config,
                        AuthService authService) {
        this.server = server;
        this.config = config;
        this.authService = authService;
    }

    @Override
    public void execute(Invocation invocation) {

        Player player = (Player) invocation.source();
        UUID uuid = player.getUniqueId();

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

        if (!authService.authenticate(uuid, password)) {

            authService.addAttempt(uuid);

            if (authService.getAttempts(uuid) >= authService.getMaxAttempts()) {
                authService.block(uuid);

                player.disconnect(Messages.parse(
                        config.getString("messages.login.kick-screen"))
                );
                return;
            }

            player.sendMessage(Messages.parse(
                    config.getString("messages.wrong-credentials")
            ));

            return;
        }

        player.sendMessage(Messages.parse(
                config.getString("messages.login.success"))
        );

        authService.resetAttempts(uuid);

        server.getServer("lobby").ifPresent(lobby ->
                player.createConnectionRequest(lobby).fireAndForget());
    }
}