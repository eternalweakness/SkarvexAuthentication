package org.skarvex.auth.core.service;

import org.skarvex.auth.core.model.User;
import org.skarvex.auth.core.repository.AuthRepository;
import org.skarvex.auth.core.repository.UserRepository;
import org.skarvex.auth.core.security.PasswordHasher;

import java.util.UUID;

public class AuthService {

    private final UserRepository users;
    private final AuthRepository repository;
    private final PasswordHasher hasher;
    private final SessionService sessions;

    public AuthService(
            UserRepository users,
            AuthRepository repository,
            PasswordHasher hasher,
            SessionService sessions
    ) {
        this.users = users;
        this.repository = repository;
        this.hasher = hasher;
        this.sessions = sessions;
    }

    public boolean register(UUID uuid, String name, String password) {
        if (users.findById(uuid).isPresent()) {
            return false;
        }

        users.save(
                new User(uuid, name, hasher.hash(password))
        );

        return true;
    }

    public boolean authenticate(UUID uuid, String password) {
        return users.findById(uuid)
                .filter(user ->
                        hasher.verify(password, user.passwordHash())
        ).map(user -> {
            sessions.login(uuid);
            return true;
                }).orElse(false);
    }

    public boolean isRegistered(UUID uuid) {
        return users.findById(uuid).isPresent();
    }

    public boolean isAuthenticated(UUID uuid) {
        return sessions.isAuthenticated(uuid);
    }

}
