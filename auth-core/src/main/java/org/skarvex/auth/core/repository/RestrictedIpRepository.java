package org.skarvex.auth.core.repository;

import org.skarvex.auth.core.model.RestrictedIp;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface RestrictedIpRepository {

    CompletableFuture<Void> block(RestrictedIp restrictedIp);

    CompletableFuture<Optional<RestrictedIp>> findByIp(String ip);

    CompletableFuture<Void> unblock(String ip);
}
