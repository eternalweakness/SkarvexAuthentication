package org.skarvex.auth.paper.manager.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.skarvex.auth.paper.AuthPaper;

import java.util.List;

public class ConfigurationManager {

    private final FileConfiguration config;

    public ConfigurationManager(AuthPaper plugin) {
        this.config = plugin.getConfig();
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
