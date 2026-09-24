package tech.opsnova.order.exception;

/**
 * Raised when catalog-service cannot be reached (connection refused, DNS
 * failure, timeout). The REST handler turns this into a clean 503; order-service
 * itself stays up and ready. This is the core Stage 3 behaviour.
 */
public class CatalogUnavailableException extends RuntimeException {

    public CatalogUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
