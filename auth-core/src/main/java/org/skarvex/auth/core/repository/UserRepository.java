package org.skarvex.auth.core.repository.user;

import org.skarvex.auth.core.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findById(UUID uuid);

    void save(User user);
}
