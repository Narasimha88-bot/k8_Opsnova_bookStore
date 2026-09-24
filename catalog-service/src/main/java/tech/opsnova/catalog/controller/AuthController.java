package tech.opsnova.catalog.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tech.opsnova.catalog.dto.LoginRequest;
import tech.opsnova.catalog.dto.LoginResponse;
import tech.opsnova.catalog.security.HardcodedUsers;
import tech.opsnova.catalog.security.JwtService;
import tech.opsnova.catalog.security.TokenRevocationService;

/**
 * JWT auth API (full profile only). catalog is the auth provider - it issues the
 * tokens order-service later validates locally.
 */
@RestController
@RequestMapping("/api/auth")
@Profile("full")
public class AuthController {

    private final HardcodedUsers users;
    private final JwtService jwtService;
    private final TokenRevocationService revocation;

    public AuthController(HardcodedUsers users, JwtService jwtService, TokenRevocationService revocation) {
        this.users = users;
        this.jwtService = jwtService;
        this.revocation = revocation;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        String role = users.authenticate(request.username(), request.password());
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return new LoginResponse(jwtService.issue(request.username(), role), request.username(), role);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            revocation.revoke(authHeader.substring(7));
        }
    }
}
