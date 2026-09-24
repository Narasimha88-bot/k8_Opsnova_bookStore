package tech.opsnova.order.exception;

/**
 * The requested bookId does not exist in catalog (catalog returned 404).
 */
public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(Long bookId) {
        super("No book with id " + bookId + " in catalog");
    }
}
