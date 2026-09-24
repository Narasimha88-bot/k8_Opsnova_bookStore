package tech.opsnova.order.security;

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
 * `full` -> the REST order API requires a valid JWT; the browser pages permit
 * through and redirect to login themselves. Other profiles -> everything open.
 */
@Configuration
public class SecurityConfig {

    @Bean
    @Profile("full")
    SecurityFilterChain fullChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/css/**", "/js/**", "/error").permitAll()
                        // Teaching hooks are deliberately open in every profile.
                        .requestMatchers("/api/admin/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/info").permitAll()
                        // Browser pages: permitted here, the controller redirects to login.
                        .requestMatchers("/", "/orders").permitAll()
                        .requestMatchers(HttpMethod.POST, "/orders").permitAll()
                        // REST API: requires a valid JWT.
                        .requestMatchers("/api/orders/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(new JwtAuthenticationFilter(jwtService),
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
