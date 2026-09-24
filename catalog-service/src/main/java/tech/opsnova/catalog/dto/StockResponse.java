package tech.opsnova.catalog.dto;

/**
 * Shape of GET /api/books/{id}/stock. Consumed by order-service (Stage 3).
 */
public record StockResponse(Long bookId, Integer stockCount) {
}
