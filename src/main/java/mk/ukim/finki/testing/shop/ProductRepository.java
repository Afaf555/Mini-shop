package mk.ukim.finki.testing.shop;

import java.util.Optional;

/** Provides access to the product catalog. */
public interface ProductRepository {

    /**
     * Finds a product by its code.
     *
     * @param code the product code
     * @return the product, or Optional.empty() if the code does not exist
     */
    Optional<Product> findByCode(String code);
}
