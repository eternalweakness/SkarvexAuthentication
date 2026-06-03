package org.skarvex.auth.core.service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionService {

    private final Set<UUID> authenticated = ConcurrentHashMap.newKeySet();

    public void login(UUID id) {
        authenticated.add(id);
    }

    public void logout(UUID id) {
        authenticated.remove(id);
    }

    public boolean isAuthenticated(UUID id){
        return authenticated.contains(id);
    }
}
