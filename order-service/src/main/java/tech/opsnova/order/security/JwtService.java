package tech.opsnova.order.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Verifies JWTs LOCALLY with the shared HMAC key (JWT_SIGNING_KEY, no default).
 * order-service never calls catalog to validate - local verification with a
 * shared secret is exactly what makes the key-rotation demo work: change the key
 * on catalog only and order-service rejects catalog's newly-signed tokens.
 */
@Service
@Profile("full")
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${JWT_SIGNING_KEY}") String signingKey) {
        this.key = Keys.hmacShaKeyFor(signingKey.getBytes(StandardCharsets.UTF_8));
    }

    /** Verifies signature + expiry. Throws io.jsonwebtoken.JwtException if invalid. */
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
