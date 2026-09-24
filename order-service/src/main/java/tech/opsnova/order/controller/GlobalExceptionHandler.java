package tech.opsnova.order.controller;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tech.opsnova.order.dto.ErrorResponse;
import tech.opsnova.order.exception.BookNotFoundException;
import tech.opsnova.order.exception.CatalogUnavailableException;
import tech.opsnova.order.exception.InsufficientStockException;
import tech.opsnova.order.exception.OrderNotFoundException;

/**
 * JSON error handling for the REST API only (@RestController beans). The
 * Thymeleaf view controller is a plain @Controller and handles its own errors
 * so the browser sees friendly banners / pages instead of JSON.
 */
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    /** The Stage 3 hard requirement: catalog down -> clean 503, never a crash. */
    @ExceptionHandler(CatalogUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleCatalogDown(CatalogUnavailableException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookNotFound(BookNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        ErrorResponse body = new ErrorResponse(
                status.value(), status.getReasonPhrase(), message, Instant.now());
        return ResponseEntity.status(status).body(body);
    }
}
