package tech.opsnova.order.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import tech.opsnova.order.security.CurrentUser;

/**
 * Adds auth state to every page. loginUrl / logoutUrl point at catalog (the auth
 * provider) - locally the full-stack compose sets APP_LOGIN_URL / APP_LOGOUT_URL
 * to catalog's URL; behind one ingress host they default to /login and /logout.
 */
@ControllerAdvice(annotations = Controller.class)
public class GlobalModelAdvice {

    @Value("${app.security.enabled:false}")
    private boolean securityEnabled;

    @Value("${app.login-url:/login}")
    private String loginUrl;

    @Value("${app.logout-url:/logout}")
    private String logoutUrl;

    @ModelAttribute
    void globals(Model model) {
        model.addAttribute("securityEnabled", securityEnabled);
        model.addAttribute("loginUrl", loginUrl);
        model.addAttribute("logoutUrl", logoutUrl);
        model.addAttribute("currentUser", CurrentUser.username());
    }
}
