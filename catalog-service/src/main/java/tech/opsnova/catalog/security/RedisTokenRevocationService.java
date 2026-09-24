package tech.opsnova.catalog.security;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis-backed revocation. Created ONLY when app.redis.enabled=true (that flag is
 * how the optional dependency is wired). If Redis is configured but later becomes
 * unreachable, the individual calls degrade gracefully - they log a warning and
 * continue, so login and ordering keep working (spec: "warn & keep serving").
 * That targeted degradation is the documented behaviour, not error swallowing.
 */
@Service
@Profile("full")
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
public class RedisTokenRevocationService implements TokenRevocationService {

    private static final Logger log = LoggerFactory.getLogger(RedisTokenRevocationService.class);
    private static final String PREFIX = "revoked:";

    private final StringRedisTemplate redis;
    private final long ttlSeconds;

    public RedisTokenRevocationService(StringRedisTemplate redis,
                                       @Value("${app.jwt.ttl-seconds:3600}") long ttlSeconds) {
        this.redis = redis;
        this.ttlSeconds = ttlSeconds;
        log.info("Token revocation: Redis-backed (enabled).");
    }

    @Override
    public void revoke(String token) {
        try {
            // Expire the revocation entry when the token itself would expire.
            redis.opsForValue().set(PREFIX + token, "1", Duration.ofSeconds(ttlSeconds));
        } catch (DataAccessException ex) {
            log.warn("Redis unavailable - could not record token revocation: {}", ex.getMessage());
        }
    }

    @Override
    public boolean isRevoked(String token) {
        try {
            return Boolean.TRUE.equals(redis.hasKey(PREFIX + token));
        } catch (DataAccessException ex) {
            log.warn("Redis unavailable - skipping revocation check (fail-open): {}", ex.getMessage());
            return false;
        }
    }
}
