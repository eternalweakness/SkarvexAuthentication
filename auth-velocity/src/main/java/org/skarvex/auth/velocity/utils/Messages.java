package org.skarvex.auth.velocity.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class Messages {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    public static Component parse(String text) {
        if (text == null) {
            return Component.text("Message missing");
        }

        return MM.deserialize(text);
    }
}
