package org.skarvex.auth.core.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.skarvex.auth.core.model.RestrictedIp;
import org.skarvex.auth.core.repository.RestrictedIpRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public class BruteForceService {

    private final RestrictedIpRepository ipRepository;

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration ATTEMPTS_TTL = Duration.ofMinutes(15);
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(10);

    private final Cache<String, Integer> failedAttemptsCache = Caffeine.newBuilder()
            .expireAfterWrite(ATTEMPTS_TTL)
            .maximumSize(10000)
            .build();

    private final Cache<String, Instant> blockedCache = Caffeine.newBuilder()
            .expireAfterWrite(BLOCK_DURATION)
            .maximumSize(10000)
            .build();

    public BruteForceService(RestrictedIpRepository ipRepository) {
        this.ipRepository = ipRepository;
    }

    public CompletableFuture<Boolean> isBlocked(String ip) {
        Instant blockedUntil = blockedCache.getIfPresent(ip);
        Instant now = Instant.now();

        if (blockedUntil != null) {
            if (blockedUntil.isAfter(now)) {
                return CompletableFuture.completedFuture(true);
            }
            blockedCache.invalidate(ip);
        }

        return ipRepository.findByIp(ip).thenApply(restrictedIp -> {
            if (restrictedIp.isEmpty()) return false;

            if (restrictedIp.get().blockedUntil().isAfter(now)) {
                blockedCache.put(ip, restrictedIp.get().blockedUntil());
                return true;
            }

            unblock(ip);
            return false;
        });
    }

    public CompletableFuture<Void> block(String ip) {
        RestrictedIp restrictedIp = new RestrictedIp(
                ip,
                Instant.now().plus(BLOCK_DURATION)
        );

        return ipRepository.block(restrictedIp).thenRun(() -> {
            blockedCache.put(restrictedIp.ip(), restrictedIp.blockedUntil());
            failedAttemptsCache.invalidate(restrictedIp.ip());
        });
    }
    public CompletableFuture<Void> unblock(String ip) {
        return ipRepository.unblock(ip);
    }

    public int increment(String ip) {
        return failedAttemptsCache.asMap()
                .compute(ip, (key, attempts)
                -> attempts == null ? 1 : ++attempts
        );
    }
    public void reset(String ip) {
        failedAttemptsCache.invalidate(ip);
    }

    public int getMaxAttempts() {
        return MAX_ATTEMPTS;
    }

    public Duration getRemainingTime(String ip) {
        return Duration.between(
                Instant.now(),
                blockedCache.getIfPresent(ip)
        );
    }
}
