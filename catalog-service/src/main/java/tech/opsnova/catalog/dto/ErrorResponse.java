package tech.opsnova.catalog.dto;

import java.time.Instant;

/**
 * Clean JSON error body returned by the global exception handler.
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        Instant timestamp) {
}
