package org.skarvex.auth.core.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LoginTimeoutService {

    private final Map<UUID, Instant> deadlines = new ConcurrentHashMap<>();

    public void start(UUID uuid, Duration duration) {
        deadlines.put(uuid, Instant.now().plus(duration));
    }

    public void cancel(UUID uuid) {
        deadlines.remove(uuid);
    }

    public boolean isExpired(UUID uuid) {
        Instant deadline = deadlines.get(uuid);

        return deadline != null && Instant.now().isAfter(deadline);
    }

    public long getRemainingSeconds(UUID uuid) {
        Instant deadline = deadlines.get(uuid);

        if (deadline == null) return 0;

        return Math.max(
                0,
                Duration.between(
                        Instant.now(),
                        deadline
                ).toSeconds()
        );
    }

    public boolean hasTimeout(UUID uuid) {
        return deadlines.containsKey(uuid);
    }

}
