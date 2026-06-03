package org.skarvex.auth.core.auth.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import org.slf4j.Logger;

public class AuthVelocity {

    @Inject private Logger logger;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
      // Plugin initialization logic goes here
    }
}
