package org.skarvex.auth.paper.manager.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.skarvex.auth.paper.AuthPaper;

import java.util.List;
import java.util.Optional;

public class ConfigurationManager {

    private final FileConfiguration config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ConfigurationManager(AuthPaper plugin) {
        this.config = plugin.getConfig();
    }

    public Component getMessage(String path) {
        return Optional.ofNullable(
                config.getString(path)
        ).map(miniMessage::deserialize)
                .orElseGet(Component::empty);
    }

    public double getDouble(String path) {
        return config.getDouble(path);
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public List<String> getList(String path) {
        return config.getStringList(path);
    }

}
