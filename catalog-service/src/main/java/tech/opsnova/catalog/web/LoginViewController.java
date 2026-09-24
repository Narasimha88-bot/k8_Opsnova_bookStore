package tech.opsnova.catalog.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tech.opsnova.catalog.security.HardcodedUsers;
import tech.opsnova.catalog.security.JwtAuthenticationFilter;
import tech.opsnova.catalog.security.JwtService;
import tech.opsnova.catalog.security.TokenRevocationService;

/**
 * Browser login (full profile). On success it drops the JWT into the
 * OPSNOVA_TOKEN cookie. The cookie is scoped to the host (not the port), so the
 * browser also sends it to order-service on the same host - that is how the
 * cross-service order flow stays logged in locally and behind one ingress host.
 */
@Controller
@Profile("full")
public class LoginViewController {

    private final HardcodedUsers users;
    private final JwtService jwtService;
    private final TokenRevocationService revocation;
    private final int ttlSeconds;

    public LoginViewController(HardcodedUsers users, JwtService jwtService, TokenRevocationService revocation,
                               @Value("${app.jwt.ttl-seconds:3600}") int ttlSeconds) {
        this.users = users;
        this.jwtService = jwtService;
        this.revocation = revocation;
        this.ttlSeconds = ttlSeconds;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username, @RequestParam String password,
                          HttpServletResponse response, RedirectAttributes ra) {
        String role = users.authenticate(username, password);
        if (role == null) {
            ra.addFlashAttribute("error", "Invalid username or password.");
            return "redirect:/login";
        }
        Cookie cookie = new Cookie(JwtAuthenticationFilter.COOKIE, jwtService.issue(username, role));
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(ttlSeconds);
        response.addCookie(cookie);
        return "redirect:/books";
    }

    @GetMapping("/logout")
    public String logout(@CookieValue(value = JwtAuthenticationFilter.COOKIE, required = false) String token,
                         HttpServletResponse response) {
        if (token != null) {
            revocation.revoke(token);
        }
        Cookie cookie = new Cookie(JwtAuthenticationFilter.COOKIE, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/books";
    }
}
