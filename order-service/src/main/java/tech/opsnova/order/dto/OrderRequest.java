package tech.opsnova.order.dto;

/**
 * POST /api/orders body. In `standalone` there is no auth, so username is taken
 * from the request (default "user"). In `full` (Stage 5) it will come from the JWT.
 */
public record OrderRequest(Long bookId, Integer quantity, String username) {
}
