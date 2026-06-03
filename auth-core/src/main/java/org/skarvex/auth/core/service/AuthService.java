package org.skarvex.auth.core.service;

import org.skarvex.auth.core.model.User;
import org.skarvex.auth.core.repository.UserRepository;
import org.skarvex.auth.core.security.PasswordHasher;

import java.util.UUID;

public class AuthService {

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final SessionService sessions;

    public AuthService(
            UserRepository users,
            PasswordHasher hasher,
            SessionService sessions
    ) {
        this.users = users;
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

        sessions.login(uuid);

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

    public boolean comparePassword(UUID uuid, String password) {
        return users.findById(uuid)
                .map(user ->
                        hasher.verify(password, user.passwordHash())
                )
                .orElse(false);
    }

    public void logout(UUID uuid) {
        sessions.logout(uuid);
    }

    public boolean isRegistered(UUID uuid) {
        return users.findById(uuid).isPresent();
    }

    public boolean isAuthenticated(UUID uuid) {
        return sessions.isAuthenticated(uuid);
    }

    public boolean updatePassword(UUID uuid, String newPassword) {
        return users.updatePassword(
                uuid,
                hasher.hash(newPassword)
        );
    }

}
