package mk.ukim.finki.testing.shop;

/**
 * Product data returned by the catalog. Stock is a snapshot, not a
 * reservation.
 *
 * @param code product code, not null or blank
 * @param unitPriceCents price of one unit in cents
 * @param availableQuantity units in stock, zero or positive
 */
public record Product(String code, int unitPriceCents,
        int availableQuantity) {

    /**
     * Validates the product data. Throws IllegalArgumentException if the
     * code is blank, the price is out of range or the stock is negative.
     */
    public Product {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Product code must not be blank");
        }
        if (unitPriceCents < PriceCalculator.MIN_UNIT_PRICE_CENTS
                || unitPriceCents > PriceCalculator.MAX_UNIT_PRICE_CENTS) {
            throw new IllegalArgumentException("Unit price must be between "
                    + PriceCalculator.MIN_UNIT_PRICE_CENTS + " and "
                    + PriceCalculator.MAX_UNIT_PRICE_CENTS + " cents");
        }
        if (availableQuantity < 0) {
            throw new IllegalArgumentException(
                    "Available quantity must not be negative");
        }
    }
}
