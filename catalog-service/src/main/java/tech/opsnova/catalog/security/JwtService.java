package tech.opsnova.catalog.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Issues and verifies JWTs with a shared HMAC key. The key comes from
 * JWT_SIGNING_KEY with NO default - if it is unset (or shorter than 256 bits)
 * the bean fails to build and the app does not start in `full`. That is the
 * "secret has no fallback" rule, and the reason the key becomes a Secret lesson.
 *
 * order-service holds an identical copy of this class and verifies tokens
 * locally with the same key - it never calls catalog to validate.
 */
@Service
@Profile("full")
public class JwtService {

    private final SecretKey key;
    private final long ttlSeconds;

    public JwtService(@Value("${JWT_SIGNING_KEY}") String signingKey,
                      @Value("${app.jwt.ttl-seconds:3600}") long ttlSeconds) {
        this.key = Keys.hmacShaKeyFor(signingKey.getBytes(StandardCharsets.UTF_8));
        this.ttlSeconds = ttlSeconds;
    }

    public String issue(String username, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(key)
                .compact();
    }

    /** Verifies signature + expiry. Throws io.jsonwebtoken.JwtException if invalid. */
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
