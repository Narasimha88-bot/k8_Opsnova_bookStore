package tech.opsnova.catalog.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Exactly one filter chain is active per profile:
 *   - `full`  -> JWT-secured: writes to books + logout require a valid token.
 *   - others  -> everything open (standalone / external-db behave as before).
 */
@Configuration
public class SecurityConfig {

    @Bean
    @Profile("full")
    SecurityFilterChain fullChain(HttpSecurity http, JwtService jwtService,
                                  TokenRevocationService revocation) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/css/**", "/js/**", "/error").permitAll()
                        // Teaching hooks are deliberately open in every profile.
                        .requestMatchers("/api/admin/**").permitAll()
                        .requestMatchers("/login", "/logout").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/", "/books", "/books/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/books", "/api/books/**", "/api/info").permitAll()
                        // Protected: writes to the catalog + logout.
                        .requestMatchers(HttpMethod.POST, "/api/books/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/books/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").authenticated()
                        .requestMatchers("/api/auth/logout").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, revocation),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @Profile("!full")
    SecurityFilterChain openChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
