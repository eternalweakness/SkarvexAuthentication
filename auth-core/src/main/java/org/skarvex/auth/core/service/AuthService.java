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

    public boolean register(UUID uuid, String password, String registrationIp, String loginIp, boolean rememberSession) {
        if (users.findById(uuid).isPresent()) {
            return false;
        }

        users.save(
                new User(uuid,
                        hasher.hash(password),
                        registrationIp,
                        loginIp,
                        rememberSession
                )
        );

        sessions.login(uuid);

        return true;
    }
    public boolean authenticate(UUID uuid, String password, String incomingIp) {
        return users.findById(uuid)
                .filter(user ->
                        hasher.verify(password, user.passwordHash())
        ).map(user -> {
            sessions.login(uuid);

            users.setAutoLogin(uuid, true);
            users.updateLastLoginIp(uuid, incomingIp);
            return true;
                }).orElse(false);
    }

    public void logout(UUID uuid) {
        sessions.logout(uuid);
    }

    /**
     * Cancels the user's active session using the logout() method, and also disables automatic session for a specific UUID.
     * @param uuid Server user UUID
     */
    public void revokeSession(UUID uuid) {
        sessions.logout(uuid);
        users.setAutoLogin(uuid, false);
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

    public boolean tryAutoLogin(UUID uuid, String incomingIp) {
        return users.findById(uuid).filter(User::autoLogin)
                .filter(user -> {
                    boolean trustedSession = incomingIp.equals(user.lastLoginIp());

                    if (!trustedSession) users.setAutoLogin(uuid, false);
                    return trustedSession;
                }).map(user -> {
                    sessions.login(uuid);
                    return true;
        }).orElse(false);
    }
}
