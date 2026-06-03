package org.skarvex.auth.core.repository;

import org.skarvex.auth.core.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findByName(String name);

    Optional<User> findById(UUID uuid);

    void save(User user);
}
