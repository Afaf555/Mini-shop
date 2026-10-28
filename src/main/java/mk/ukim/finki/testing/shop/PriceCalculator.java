package mk.ukim.finki.testing.shop;

/**
 * Calculates the total for an order containing one product type.
 * All amounts are in cents.
 */
public final class PriceCalculator {

    /** Smallest allowed unit price, in cents. */
    public static final int MIN_UNIT_PRICE_CENTS = 1;

    /** Largest allowed unit price, in cents. */
    public static final int MAX_UNIT_PRICE_CENTS = 1_000_000;

    /** Smallest allowed order quantity. */
    public static final int MIN_QUANTITY = 1;

    /** Largest allowed order quantity. */
    public static final int MAX_QUANTITY = 100;

    /** Percentage of the subtotal that a premium customer pays. */
    private static final long PREMIUM_PAYS_PERCENT = 90L;

    /** Base used to turn a percentage into a fraction. */
    private static final long PERCENT_BASE = 100L;

    /** Subtotals at or above this value (after discount) ship for free. */
    private static final int FREE_SHIPPING_THRESHOLD_CENTS = 5_000;

    /** Shipping cost, in cents, when shipping is not free. */
    private static final int SHIPPING_COST_CENTS = 500;

    /**
     * Calculates the order total including discount and shipping. Throws
     * IllegalArgumentException if the price or quantity is out of range.
     *
     * @param unitPriceCents price of one unit in cents
     * @param quantity number of units
     * @param premiumCustomer whether the premium discount applies
     * @return the total in cents
     */
    public int calculateTotal(final int unitPriceCents, final int quantity,
            final boolean premiumCustomer) {
        if (unitPriceCents < MIN_UNIT_PRICE_CENTS
                || unitPriceCents > MAX_UNIT_PRICE_CENTS) {
            throw new IllegalArgumentException("Unit price must be between "
                    + MIN_UNIT_PRICE_CENTS + " and " + MAX_UNIT_PRICE_CENTS
                    + " cents");
        }
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be between "
                    + MIN_QUANTITY + " and " + MAX_QUANTITY);
        }

        int subtotal = unitPriceCents * quantity;
        int discountedSubtotal = subtotal;
        if (premiumCustomer) {
            discountedSubtotal =
                    (int) (subtotal * PREMIUM_PAYS_PERCENT / PERCENT_BASE);
        }

        int shipping = SHIPPING_COST_CENTS;
        if (discountedSubtotal >= FREE_SHIPPING_THRESHOLD_CENTS) {
            shipping = 0;
        }
        return discountedSubtotal + shipping;
    }
}
