package tech.opsnova.catalog.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 404. The @ResponseStatus makes the browser (view controller) render the HTML
 * error page with a 404; the REST API handler returns the same status as JSON.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(Long id) {
        super("Book " + id + " not found");
    }
}
