package org.skarvex.auth.paper.manager.title;

import net.kyori.adventure.title.Title;
import org.skarvex.auth.paper.manager.config.ConfigurationManager;

public class TitleManager {

    private final ConfigurationManager config;

    public TitleManager(ConfigurationManager config) {
        this.config = config;
    }

    public Title getTitle() {
        return Title.title(
                config.getMessage("auth.title"),
                config.getMessage("auth.subtitle")
        );
    }
}
