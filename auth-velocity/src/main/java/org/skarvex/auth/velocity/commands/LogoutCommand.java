package org.skarvex.auth.velocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.skarvex.auth.core.service.AuthService;
import org.skarvex.auth.velocity.manager.ConfigurationManager;
import org.skarvex.auth.velocity.utils.Messages;

import java.util.UUID;

public class LogoutCommand implements SimpleCommand {

    private final ProxyServer server;
    private final ConfigurationManager config;
    private final AuthService authService;

    public LogoutCommand(ProxyServer server,
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

        if (arguments.length > 0) {
            player.sendMessage(Messages.parse(config.getString
                    ("messages.logout.usage"))
            );

            return;
        }

        if (!authService.isRegistered(uuid)) {
            player.sendMessage(Messages.parse(
                    config.getString("messages.not-registered")
            ));
            return;
        }

        if (!authService.isAuthenticated(uuid)) {
            player.sendMessage(Messages.parse(
                    config.getString("messages.not-logged")
            ));
            return;
        }

        authService.revokeSession(uuid);

        player.disconnect(Messages.parse(
                    config.getString("messages.logout.kick-screen"))
            );
    }
}
