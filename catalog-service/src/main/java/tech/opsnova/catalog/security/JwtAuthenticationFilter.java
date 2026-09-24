package tech.opsnova.catalog.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Reads the JWT from the Authorization: Bearer header (API clients) or the
 * OPSNOVA_TOKEN cookie (browser), verifies it, checks revocation, and populates
 * the SecurityContext. An invalid/expired/revoked token simply leaves the
 * request unauthenticated - the endpoint rules then decide 401/403.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String COOKIE = "OPSNOVA_TOKEN";

    private final JwtService jwtService;
    private final TokenRevocationService revocation;

    public JwtAuthenticationFilter(JwtService jwtService, TokenRevocationService revocation) {
        this.jwtService = jwtService;
        this.revocation = revocation;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (!revocation.isRevoked(token)) {
                    Claims claims = jwtService.parse(token);
                    String role = String.valueOf(claims.get("role"));
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            claims.getSubject(), null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (JwtException ex) {
                // Bad signature / expired token -> stay unauthenticated.
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
