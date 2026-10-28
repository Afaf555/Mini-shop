package mk.ukim.finki.testing.shop;

/** Sends order confirmations to customers. */
public interface OrderNotifier {

    /**
     * Sends a confirmation for the order. Implementations may throw
     * IllegalStateException if the notification cannot be delivered.
     *
     * @param order the order to confirm
     */
    void sendConfirmation(Order order);
}
