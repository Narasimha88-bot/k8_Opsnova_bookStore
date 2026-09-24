package tech.opsnova.order.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tech.opsnova.order.dto.OrderRequest;
import tech.opsnova.order.model.Order;
import tech.opsnova.order.security.CurrentUser;
import tech.opsnova.order.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order place(@RequestBody(required = false) OrderRequest request) {
        if (request == null || request.bookId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "bookId is required");
        }
        int quantity = request.quantity() == null ? 1 : request.quantity();
        if (quantity < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be at least 1");
        }
        // In `full` the username is the authenticated JWT subject; otherwise it
        // comes from the request (default "user").
        String authenticated = CurrentUser.username();
        String username = authenticated != null ? authenticated
                : (StringUtils.hasText(request.username()) ? request.username() : "user");
        return orderService.placeOrder(username, request.bookId(), quantity);
    }

    @GetMapping
    public List<Order> list(@RequestParam(required = false) String username) {
        // In `full` this narrows to the authenticated caller's orders; otherwise
        // it lists all (or by an explicit username filter).
        String authenticated = CurrentUser.username();
        if (authenticated != null) {
            return orderService.findByUsername(authenticated);
        }
        return StringUtils.hasText(username)
                ? orderService.findByUsername(username)
                : orderService.findAll();
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable Long id) {
        return orderService.findById(id);
    }
}
