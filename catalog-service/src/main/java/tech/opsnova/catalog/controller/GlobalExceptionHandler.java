package tech.opsnova.catalog.controller;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tech.opsnova.catalog.dto.ErrorResponse;
import tech.opsnova.catalog.exception.BookNotFoundException;

/**
 * JSON error handling for the REST API only (@RestController beans). The
 * Thymeleaf view controller is a plain @Controller and is intentionally NOT
 * covered here, so a missing book in the browser renders the HTML error page
 * instead of a JSON body.
 */
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(BookNotFoundException ex) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
