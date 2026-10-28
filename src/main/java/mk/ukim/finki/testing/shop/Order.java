package mk.ukim.finki.testing.shop;

/**
 * Immutable order data created by OrderService. All amounts are cents.
 *
 * @param customerId identifier of the customer
 * @param productCode code of the ordered product
 * @param quantity number of ordered units
 * @param totalCents order total in cents
 */
public record Order(String customerId, String productCode, int quantity,
        int totalCents) {
}
