package tech.opsnova.catalog.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Used when Redis is not enabled (app.redis.enabled missing or false). Token
 * revocation is silently disabled - logout still succeeds, tokens just live out
 * their expiry. The @ConditionalOnProperty split (this vs the Redis bean) is the
 * "optional dependency" the spec asks for.
 */
@Service
@Profile("full")
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false", matchIfMissing = true)
public class NoopTokenRevocationService implements TokenRevocationService {

    private static final Logger log = LoggerFactory.getLogger(NoopTokenRevocationService.class);

    public NoopTokenRevocationService() {
        log.warn("Token revocation: DISABLED (Redis not enabled). Logout will not revoke tokens.");
    }

    @Override
    public void revoke(String token) {
        // no-op
    }

    @Override
    public boolean isRevoked(String token) {
        return false;
    }
}
