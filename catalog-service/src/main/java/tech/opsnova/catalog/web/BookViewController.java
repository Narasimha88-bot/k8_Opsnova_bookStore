package tech.opsnova.catalog.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tech.opsnova.catalog.model.Book;
import tech.opsnova.catalog.service.BookService;

/**
 * Server-rendered pages (Thymeleaf). The pod / version / profile data shown in
 * the footer is provided globally by the {@code podInfo} bean and referenced
 * directly in the templates as {@code ${@podInfo.*}}, so these handlers only
 * deal with book data.
 */
@Controller
public class BookViewController {

    private final BookService bookService;

    /**
     * Browser-facing URL of the order flow. Defaults to /orders (ingress-relative,
     * so it resolves to order-service on the same host in Kubernetes). Locally it
     * is set to the order-service URL via APP_ORDER_URL.
     */
    @Value("${app.order-url:/orders}")
    private String orderUrl;

    public BookViewController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/books";
    }

    @GetMapping("/books")
    public String list(Model model) {
        model.addAttribute("books", bookService.findAll());
        return "books";
    }

    @GetMapping("/books/{id}")
    public String detail(@PathVariable Long id, Model model) {
        // Throws BookNotFoundException (@ResponseStatus 404) -> HTML error page.
        Book book = bookService.findById(id);
        model.addAttribute("book", book);
        model.addAttribute("orderUrl", orderUrl);
        return "book-detail";
    }
}
