package tech.opsnova.order.dto;

/**
 * Mirror of catalog's GET /api/books/{id}/stock response.
 */
public record StockResponse(Long bookId, Integer stockCount) {
}
