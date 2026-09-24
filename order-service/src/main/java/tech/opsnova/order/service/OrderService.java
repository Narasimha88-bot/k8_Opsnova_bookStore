package tech.opsnova.order.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import tech.opsnova.order.client.CatalogClient;
import tech.opsnova.order.dto.CatalogBook;
import tech.opsnova.order.dto.StockResponse;
import tech.opsnova.order.exception.InsufficientStockException;
import tech.opsnova.order.exception.OrderNotFoundException;
import tech.opsnova.order.model.Order;
import tech.opsnova.order.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CatalogClient catalogClient;

    public OrderService(OrderRepository orderRepository, CatalogClient catalogClient) {
        this.orderRepository = orderRepository;
        this.catalogClient = catalogClient;
    }

    /**
     * Places an order after verifying stock with catalog-service. We only VERIFY
     * stock (no mutation of catalog) - the simplest thing that teaches the
     * inter-service call. If catalog is unreachable the CatalogClient raises
     * CatalogUnavailableException and no order row is written.
     */
    public Order placeOrder(String username, Long bookId, int quantity) {
        StockResponse stock = catalogClient.getStock(bookId);
        int available = stock.stockCount() == null ? 0 : stock.stockCount();
        if (available < quantity) {
            throw new InsufficientStockException(bookId, quantity, available);
        }

        CatalogBook book = catalogClient.getBook(bookId);
        BigDecimal total = book.price().multiply(BigDecimal.valueOf(quantity));

        Order order = new Order();
        order.setBookId(bookId);
        order.setBookTitle(book.title());
        order.setUsername(username);
        order.setQuantity(quantity);
        order.setTotalPrice(total);
        order.setStatus("CONFIRMED");
        order.setCreatedAt(Instant.now());
        return orderRepository.save(order);
    }

    public List<Order> findAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Order> findByUsername(String username) {
        return orderRepository.findByUsernameOrderByCreatedAtDesc(username);
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
