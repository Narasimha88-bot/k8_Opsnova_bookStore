package tech.opsnova.order.dto;

import java.time.Instant;

/**
 * Clean JSON error body. The 503 returned when catalog is unreachable uses this.
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        Instant timestamp) {
}
