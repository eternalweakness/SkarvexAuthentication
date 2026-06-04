package org.skarvex.auth.core.service;

import org.skarvex.auth.core.model.User;
import org.skarvex.auth.core.repository.UserRepository;
import org.skarvex.auth.core.security.PasswordHasher;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final SessionService sessions;

    private final Map<UUID, Integer> attempts = new ConcurrentHashMap<>();
    private final Map<UUID, Instant> blockedUsers = new ConcurrentHashMap<>();

    private final int maxAttempts = 5;

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
    public void logout(UUID uuid) {
        sessions.logout(uuid);
    }

    public boolean isRegistered(UUID uuid) {
        return users.findById(uuid).isPresent();
    }
    public boolean isAuthenticated(UUID uuid) {
        return sessions.isAuthenticated(uuid);
    }

    public boolean comparePassword(UUID uuid, String password) {
        return users.findById(uuid)
                .map(user ->
                        hasher.verify(password, user.passwordHash())
                )
                .orElse(false);
    }
    public boolean updatePassword(UUID uuid, String newPassword) {
        return users.updatePassword(
                uuid,
                hasher.hash(newPassword)
        );
    }

    public int getAttempts(UUID uuid) {
        return attempts.getOrDefault(uuid, 0);
    }
    public void addAttempt(UUID uuid) {
        attempts.merge(uuid, 1, Integer::sum);
    }
    public void resetAttempts(UUID uuid) {
        attempts.remove(uuid);
    }

    public void block(UUID uuid) {
        blockedUsers.put(uuid, Instant.now().plus(Duration.ofMinutes(5)));
    }
    public boolean isBlocked(UUID uuid) {

        Instant blockedUntil = blockedUsers.get(uuid);

        if (blockedUntil == null) {
            return false;
        }

        if (Instant.now().isAfter(blockedUntil)) {
            blockedUsers.remove(uuid);
            resetAttempts(uuid);
            return false;
        }

        return true;
    }
    public long getRemainingTime(UUID uuid) {
        Instant blockedUntil = blockedUsers.get(uuid);

        if (blockedUntil == null) {
            return 0;
        }

        return Duration.between(
                Instant.now(), blockedUntil
        ).toMinutes();
    }

    // Getters

    public int getMaxAttempts() {
        return this.maxAttempts;
    }

}
