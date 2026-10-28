package mk.ukim.finki.testing.shop;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private ProductRepository products;

    @Mock
    private OrderRepository orders;

    @Mock
    private OrderNotifier notifier;

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(products, orders, notifier, new PriceCalculator());
    }

    @Test
    void placesOrderForStandardCustomer() {
        when(products.findByCode("P1")).thenReturn(Optional.of(new Product("P1", 1000, 10)));

        Order order = service.placeOrder("c1", "P1", 3, false);

        assertEquals(new Order("c1", "P1", 3, 3500), order);
        verify(products).findByCode("P1");
        verify(orders).save(order);
        verify(notifier).sendConfirmation(order);
    }

    @Test
    void placesOrderForPremiumCustomerWithDiscountAndFreeShipping() {
        when(products.findByCode("P1")).thenReturn(Optional.of(new Product("P1", 1000, 10)));

        Order order = service.placeOrder("c1", "P1", 6, true);

        assertEquals(5400, order.totalCents());
        verify(orders).save(order);
        verify(notifier).sendConfirmation(order);
    }

    @Test
    void allowsOrderingExactlyTheAvailableQuantity() {
        when(products.findByCode("P1")).thenReturn(Optional.of(new Product("P1", 1000, 5)));

        Order order = service.placeOrder("c1", "P1", 5, false);

        assertEquals(5, order.quantity());
        verify(orders).save(order);
    }

    @Test
    void savesOrderBeforeSendingConfirmation() {
        when(products.findByCode("P1")).thenReturn(Optional.of(new Product("P1", 1000, 10)));

        Order order = service.placeOrder("c1", "P1", 1, false);

        InOrder inOrder = inOrder(orders, notifier);
        inOrder.verify(orders).save(order);
        inOrder.verify(notifier).sendConfirmation(order);
    }

    @ParameterizedTest
    @ValueSource(strings = { " p1", "P1 ", "AbC" })
    void usesProductCodeExactlyAsSupplied(String code) {
        when(products.findByCode(code)).thenReturn(Optional.of(new Product(code, 1000, 10)));

        Order order = service.placeOrder("c1", code, 1, false);

        assertEquals(code, order.productCode());
        verify(products).findByCode(code);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "   ", "\t", "\n" })
    void rejectsBlankCustomerId(String customerId) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.placeOrder(customerId, "P1", 1, false));

        assertTrue(exception.getMessage().contains("Customer ID"));
        verifyNoInteractions(products, orders, notifier);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "   ", "\t", "\n" })
    void rejectsBlankProductCode(String productCode) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.placeOrder("c1", productCode, 1, false));

        assertTrue(exception.getMessage().contains("Product code"));
        verifyNoInteractions(products, orders, notifier);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1, 101, Integer.MIN_VALUE, Integer.MAX_VALUE })
    void rejectsQuantityOutsideRange(int quantity) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.placeOrder("c1", "P1", quantity, false));

        assertTrue(exception.getMessage().contains("Quantity"));
        verifyNoInteractions(products, orders, notifier);
    }

    @Test
    void validatesCustomerIdBeforeProductCodeAndQuantity() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.placeOrder(" ", null, 0, false));

        assertTrue(exception.getMessage().contains("Customer ID"));
    }

    @Test
    void validatesProductCodeBeforeQuantity() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.placeOrder("c1", " ", 0, false));

        assertTrue(exception.getMessage().contains("Product code"));
    }

    @Test
    void throwsWhenProductIsMissing() {
        when(products.findByCode("X")).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> service.placeOrder("c1", "X", 1, false));

        verify(products).findByCode("X");
        verifyNoInteractions(orders, notifier);
    }

    @Test
    void throwsWhenStockIsInsufficient() {
        when(products.findByCode("P1")).thenReturn(Optional.of(new Product("P1", 1000, 5)));

        assertThrows(InsufficientStockException.class, () -> service.placeOrder("c1", "P1", 6, false));

        verifyNoInteractions(orders, notifier);
    }

    @Test
    void throwsWhenProductIsOutOfStock() {
        when(products.findByCode("P1")).thenReturn(Optional.of(new Product("P1", 1000, 0)));

        assertThrows(InsufficientStockException.class, () -> service.placeOrder("c1", "P1", 1, false));

        verifyNoInteractions(orders, notifier);
    }

    @Test
    void doesNotSendConfirmationWhenSavingFails() {
        when(products.findByCode("P1")).thenReturn(Optional.of(new Product("P1", 1000, 10)));
        IllegalStateException failure = new IllegalStateException("cannot save");
        doThrow(failure).when(orders).save(any(Order.class));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.placeOrder("c1", "P1", 1, false));

        assertEquals(failure, exception);
        verifyNoInteractions(notifier);
    }

    @Test
    void propagatesNotificationFailureAfterOrderWasSaved() {
        when(products.findByCode("P1")).thenReturn(Optional.of(new Product("P1", 1000, 10)));
        IllegalStateException failure = new IllegalStateException("cannot notify");
        doThrow(failure).when(notifier).sendConfirmation(any(Order.class));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.placeOrder("c1", "P1", 1, false));

        assertEquals(failure, exception);
        verify(orders).save(any(Order.class));
    }

    @Test
    void rejectsNullDependencies() {
        PriceCalculator calculator = new PriceCalculator();

        assertThrows(NullPointerException.class, () -> new OrderService(null, orders, notifier, calculator));
        assertThrows(NullPointerException.class, () -> new OrderService(products, null, notifier, calculator));
        assertThrows(NullPointerException.class, () -> new OrderService(products, orders, null, calculator));
        assertThrows(NullPointerException.class, () -> new OrderService(products, orders, notifier, null));
    }

}
