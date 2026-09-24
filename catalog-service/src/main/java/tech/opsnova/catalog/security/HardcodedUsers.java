package tech.opsnova.catalog.security;

import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Two hardcoded users - a user table adds no teaching value (spec 7.1).
 * admin/admin123 (ADMIN), user/user123 (USER).
 */
@Component
@Profile("full")
public class HardcodedUsers {

    private record Cred(String password, String role) {
    }

    private final Map<String, Cred> users = Map.of(
            "admin", new Cred("admin123", "ADMIN"),
            "user", new Cred("user123", "USER"));

    /** Returns the role when the credentials match, otherwise null. */
    public String authenticate(String username, String password) {
        Cred cred = users.get(username);
        return (cred != null && cred.password().equals(password)) ? cred.role() : null;
    }
}
