package tech.opsnova.order.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tech.opsnova.order.exception.BookNotFoundException;
import tech.opsnova.order.exception.CatalogUnavailableException;
import tech.opsnova.order.exception.InsufficientStockException;
import tech.opsnova.order.model.Order;
import tech.opsnova.order.security.CurrentUser;
import tech.opsnova.order.service.OrderService;

/**
 * Server-rendered order-history page. In `full` these pages require a login:
 * when there is no authenticated user they redirect to catalog's login page.
 * Catalog failures are still caught and shown as a friendly banner.
 */
@Controller
public class OrderViewController {

    private final OrderService orderService;

    @Value("${app.security.enabled:false}")
    private boolean securityEnabled;

    @Value("${app.login-url:/login}")
    private String loginUrl;

    public OrderViewController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/orders";
    }

    @GetMapping("/orders")
    public String list(@RequestParam(required = false) Long bookId, Model model) {
        String user = CurrentUser.username();
        if (securityEnabled && user == null) {
            return "redirect:" + loginUrl;
        }
        model.addAttribute("orders", user != null ? orderService.findByUsername(user) : orderService.findAll());
        model.addAttribute("prefillBookId", bookId != null ? bookId : 1L);
        return "orders";
    }

    @PostMapping("/orders")
    public String place(@RequestParam Long bookId,
                        @RequestParam(defaultValue = "1") int quantity,
                        @RequestParam(required = false) String username,
                        RedirectAttributes ra) {
        String authenticated = CurrentUser.username();
        if (securityEnabled && authenticated == null) {
            return "redirect:" + loginUrl;
        }
        String user = authenticated != null ? authenticated
                : (StringUtils.hasText(username) ? username : "user");
        try {
            Order order = orderService.placeOrder(user, bookId, quantity);
            ra.addFlashAttribute("flash", "Order #" + order.getId() + " confirmed for " + order.getBookTitle() + ".");
            ra.addFlashAttribute("flashType", "ok");
        } catch (CatalogUnavailableException e) {
            ra.addFlashAttribute("flash",
                    "Catalog is unavailable right now (503). order-service stayed up - try again shortly.");
            ra.addFlashAttribute("flashType", "error");
        } catch (BookNotFoundException e) {
            ra.addFlashAttribute("flash", "No book with id " + bookId + " in the catalog.");
            ra.addFlashAttribute("flashType", "error");
        } catch (InsufficientStockException e) {
            ra.addFlashAttribute("flash", e.getMessage());
            ra.addFlashAttribute("flashType", "error");
        }
        return "redirect:/orders";
    }
}
