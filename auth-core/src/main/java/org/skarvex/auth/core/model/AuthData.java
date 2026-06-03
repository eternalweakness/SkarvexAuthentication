package org.skarvex.auth.core.model;

import java.time.Instant;
import java.util.UUID;

public record AuthData(UUID uuid,
                       String lastIp,
                       Instant lastLogin) {}
