package mk.ukim.finki.testing.shop;

/** Thrown when the requested quantity exceeds the available stock. */
public class InsufficientStockException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param requested the requested quantity
     * @param available the quantity in stock
     */
    public InsufficientStockException(final int requested,
            final int available) {
        super("Requested quantity " + requested
                + " exceeds available quantity " + available);
    }
}
