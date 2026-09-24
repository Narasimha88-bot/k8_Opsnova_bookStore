package tech.opsnova.order.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;

/**
 * An order. Table is named "orders" because ORDER is a reserved SQL word.
 * order-service stores only bookId (not the book's title) - it shares nothing
 * with catalog except the HTTP calls made at placement time.
 */
@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookId;
    private String bookTitle;   // tool name, captured from catalog at placement time
    private String username;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String status;
    private Instant createdAt;
}
