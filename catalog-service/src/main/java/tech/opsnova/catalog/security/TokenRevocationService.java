package tech.opsnova.catalog.security;

/**
 * Token revocation. Two implementations are wired via @ConditionalOnProperty:
 * a Redis-backed one when app.redis.enabled=true, otherwise a no-op. This is
 * how the optional Redis dependency is made optional - not with try/catch.
 */
public interface TokenRevocationService {

    void revoke(String token);

    boolean isRevoked(String token);
}
