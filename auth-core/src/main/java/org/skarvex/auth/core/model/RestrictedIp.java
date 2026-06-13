package org.skarvex.auth.core.model;

import java.time.Instant;

public record RestrictedIp(
        String ip,
        Instant blockedUntil
) {
}
