package tech.opsnova.order.dto;

import java.math.BigDecimal;

/**
 * Partial view of catalog's GET /api/books/{id} response - only the fields
 * order-service needs (the price, to compute the order total). Unknown JSON
 * fields (author, isbn) are ignored by Jackson.
 */
public record CatalogBook(Long id, String title, BigDecimal price, Integer stockCount) {
}
