package mk.ukim.finki.testing.shop;

/** Thrown when a product code does not exist in the catalog. */
public class ProductNotFoundException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param productCode the code that was not found
     */
    public ProductNotFoundException(final String productCode) {
        super("Product not found: " + productCode);
    }
}
