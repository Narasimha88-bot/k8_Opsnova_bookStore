package tech.opsnova.catalog.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.stereotype.Controller;

/**
 * Adds auth state to every server-rendered page so the header can show a
 * Log in / Log out control. securityEnabled is false outside the full profile,
 * so the auth UI simply does not appear in standalone / external-db.
 */
@ControllerAdvice(annotations = Controller.class)
public class GlobalModelAdvice {

    @Value("${app.security.enabled:false}")
    private boolean securityEnabled;

    @ModelAttribute
    void globals(Model model) {
        model.addAttribute("securityEnabled", securityEnabled);
        model.addAttribute("loginUrl", "/login");
        model.addAttribute("logoutUrl", "/logout");

        String currentUser = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            currentUser = auth.getName();
        }
        model.addAttribute("currentUser", currentUser);
    }
}
