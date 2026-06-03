package org.skarvex.auth.core.repository;

import org.skarvex.auth.core.model.AuthData;

import java.util.Optional;
import java.util.UUID;

public interface AuthRepository {

    Optional<AuthData> findByUniqueId(UUID uniqueId);

    void save(AuthData data);

}
