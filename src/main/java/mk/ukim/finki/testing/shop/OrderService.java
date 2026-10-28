package mk.ukim.finki.testing.shop;

import java.util.Objects;

/** Places customer orders using the product catalog and order storage. */
public class OrderService {

    /** Product catalog. */
    private final ProductRepository products;

    /** Order storage. */
    private final OrderRepository orders;

    /** Confirmation sender. */
    private final OrderNotifier notifier;

    /** Price calculator. */
    private final PriceCalculator calculator;

    /**
     * Creates the service.
     *
     * @param productRepository product catalog
     * @param orderRepository order storage
     * @param orderNotifier confirmation sender
     * @param priceCalculator price calculator
     */
    public OrderService(final ProductRepository productRepository,
                        final OrderRepository orderRepository,
                        final OrderNotifier orderNotifier,
                        final PriceCalculator priceCalculator) {
        this.products = Objects.requireNonNull(productRepository);
        this.orders = Objects.requireNonNull(orderRepository);
        this.notifier = Objects.requireNonNull(orderNotifier);
        this.calculator = Objects.requireNonNull(priceCalculator);
    }

    /**
     * Validates the request, saves the order and sends a confirmation.
     * Throws IllegalArgumentException for invalid input,
     * ProductNotFoundException for an unknown product and
     * InsufficientStockException if there is not enough stock.
     *
     * @param customerId customer identifier, not blank
     * @param productCode product code, not blank
     * @param quantity requested quantity
     * @param premiumCustomer whether the premium discount applies
     * @return the saved order
     */
    public Order placeOrder(final String customerId,
                            final String productCode, final int quantity,
                            final boolean premiumCustomer) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer ID must not be blank");
        }
        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Product code must not be blank");
        }
        if (quantity < PriceCalculator.MIN_QUANTITY
                || quantity > PriceCalculator.MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be between "
                    + PriceCalculator.MIN_QUANTITY + " and "
                    + PriceCalculator.MAX_QUANTITY);
        }

        Product product = products.findByCode(productCode)
                .orElseThrow(() -> new ProductNotFoundException(productCode));
        if (quantity > product.availableQuantity()) {
            throw new InsufficientStockException(quantity,
                    product.availableQuantity());
        }

        int total = calculator.calculateTotal(product.unitPriceCents(),
                quantity, premiumCustomer);
        Order order = new Order(customerId, productCode, quantity, total);
        orders.save(order);
        notifier.sendConfirmation(order);
        return order;
    }
}
