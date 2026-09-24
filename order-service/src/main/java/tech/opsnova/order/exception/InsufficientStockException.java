package tech.opsnova.order.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(Long bookId, int requested, int available) {
        super("Insufficient stock for book " + bookId
                + ": requested " + requested + ", available " + available);
    }
}
