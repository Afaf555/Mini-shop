package mk.ukim.finki.testing.shop;

/** Stores orders. */
public interface OrderRepository {

    /**
     * Saves the order. Implementations may throw IllegalStateException if
     * the order cannot be saved.
     *
     * @param order the order to save
     */
    void save(Order order);
}
