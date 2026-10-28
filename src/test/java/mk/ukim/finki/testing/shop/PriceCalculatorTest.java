package mk.ukim.finki.testing.shop;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriceCalculatorTest {

    private PriceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriceCalculator();
    }

    @ParameterizedTest(name = "standard: price {0}, quantity {1} -> {2}")
    @CsvSource({ "4999, 1, 5499", "5000, 1, 5000", "5001, 1, 5001", "2499, 2, 5498", "2500, 2, 5000",
            "1000, 4, 4500", "1000, 5, 5000", "49, 100, 5400", "50, 100, 5000", "1, 1, 501", "1, 100, 600",
            "1000000, 1, 1000000", "1000000, 100, 100000000" })
    void calculatesTotalForStandardCustomer(int price, int quantity, int expectedTotal) {
        assertEquals(expectedTotal, calculator.calculateTotal(price, quantity, false));
    }

    @ParameterizedTest(name = "premium: price {0}, quantity {1} -> {2}")
    @CsvSource({ "1, 1, 500", "10, 1, 509", "99, 1, 589", "5000, 1, 5000", "5555, 1, 5499", "5556, 1, 5000",
            "1000, 5, 5000", "1000, 6, 5400", "5000, 2, 9000", "1000000, 100, 90000000" })
    void calculatesTotalForPremiumCustomer(int price, int quantity, int expectedTotal) {
        assertEquals(expectedTotal, calculator.calculateTotal(price, quantity, true));
    }

    @Test
    void roundsPremiumDiscountDown() {
        // 99 * 90 / 100 = 89.1 -> 89, plus 500 shipping
        assertEquals(589, calculator.calculateTotal(99, 1, true));
    }

    @Test
    void premiumDiscountIsAppliedBeforeShippingDecision() {
        // subtotal 5000 is free shipping for a standard customer,
        // but after the discount it is 4500, so shipping is added
        assertEquals(5000, calculator.calculateTotal(5000, 1, false));
        assertEquals(5000, calculator.calculateTotal(5000, 1, true));
    }

    @Test
    void doesNotOverflowForLargestOrder() {
        assertEquals(100_000_000, calculator.calculateTotal(1_000_000, 100, false));
        assertEquals(90_000_000, calculator.calculateTotal(1_000_000, 100, true));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1, 1_000_001, Integer.MIN_VALUE, Integer.MAX_VALUE })
    void rejectsUnitPriceOutsideRange(int price) {
        IllegalArgumentException standard = assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateTotal(price, 1, false));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTotal(price, 1, true));

        assertTrue(standard.getMessage().contains("Unit price"));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1, 101, Integer.MIN_VALUE, Integer.MAX_VALUE })
    void rejectsQuantityOutsideRange(int quantity) {
        IllegalArgumentException standard = assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateTotal(1000, quantity, false));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTotal(1000, quantity, true));

        assertTrue(standard.getMessage().contains("Quantity"));
    }

    @ParameterizedTest
    @CsvSource({ "1, 1", "1, 100", "1000000, 1", "1000000, 100" })
    void acceptsBoundaryValues(int price, int quantity) {
        assertDoesNotThrow(() -> calculator.calculateTotal(price, quantity, false));
        assertDoesNotThrow(() -> calculator.calculateTotal(price, quantity, true));
    }

    @ParameterizedTest(name = "price {0}, quantity {1}")
    @MethodSource("validOrders")
    void premiumCustomerNeverPaysMoreThanStandardCustomer(int price, int quantity) {
        int standard = calculator.calculateTotal(price, quantity, false);
        int premium = calculator.calculateTotal(price, quantity, true);

        assertTrue(premium <= standard);
        assertTrue(premium > 0);
    }

    static Stream<Arguments> validOrders() {
        return Stream.of(Arguments.of(1, 1), Arguments.of(99, 50), Arguments.of(4999, 1), Arguments.of(5000, 1),
                Arguments.of(5555, 1), Arguments.of(5556, 1), Arguments.of(12_345, 7),
                Arguments.of(1_000_000, 100));
    }

}
