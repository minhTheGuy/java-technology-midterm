package com.minh.jewerlystore.service;

import com.minh.jewerlystore.entity.Order;
import com.minh.jewerlystore.entity.OrderStatus;
import com.minh.jewerlystore.entity.Product;
import com.minh.jewerlystore.entity.User;
import com.minh.jewerlystore.exception.InsufficientStockException;
import com.minh.jewerlystore.payload.request.CheckoutRequest;
import com.minh.jewerlystore.payload.response.CartResponse;
import com.minh.jewerlystore.repository.OrderRepository;
import com.minh.jewerlystore.repository.ProductRepository;
import com.minh.jewerlystore.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private Product testProduct;
    private Order testOrder;
    private CheckoutRequest checkoutRequest;
    private CartResponse cartResponse;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        // Setup test product
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("50.00"));
        testProduct.setStockQuantity(10);

        // Setup test order
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setTotalAmount(new BigDecimal("100.00"));

        // Setup checkout request
        checkoutRequest = new CheckoutRequest();
        checkoutRequest.setShippingAddress("123 Test St");
        checkoutRequest.setShippingCity("Test City");
        checkoutRequest.setShippingState("Test State");
        checkoutRequest.setShippingZip("12345");
        checkoutRequest.setShippingCountry("Test Country");
        checkoutRequest.setPaymentMethod("CREDIT_CARD");

        // Setup cart response
        CartResponse.CartItemResponse cartItem = new CartResponse.CartItemResponse();
        cartItem.setProductId(1L);
        cartItem.setQuantity(2);
        cartItem.setProductPrice(new BigDecimal("50.00"));

        cartResponse = new CartResponse();
        cartResponse.setItems(Collections.singletonList(cartItem));
    }

    @Test
    void createOrder_Success() {
        // Arrange
        when(cartService.getCart(testUser)).thenReturn(cartResponse);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        Order result = orderService.createOrder(testUser, checkoutRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        assertEquals(testOrder.getStatus(), result.getStatus());

        // Verify
        verify(cartService).getCart(testUser);
        verify(productRepository).save(testProduct);
        verify(orderRepository).save(any(Order.class));
        verify(cartService).clearCart(testUser);

        // Verify product stock was updated
        assertEquals(8, testProduct.getStockQuantity());
    }

    @Test
    void createOrder_InsufficientStock() {
        // Arrange
        testProduct.setStockQuantity(1); // Set stock lower than cart quantity
        when(cartService.getCart(testUser)).thenReturn(cartResponse);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(InsufficientStockException.class, () -> {
            orderService.createOrder(testUser, checkoutRequest);
        });

        // Verify
        verify(cartService).getCart(testUser);
        verify(productRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
        verify(cartService, never()).clearCart(testUser);
    }

    @Test
    void getUserOrders_Success() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByUserOrderByOrderDateDesc(testUser)).thenReturn(orders);

        // Act
        List<Order> result = orderService.getUserOrders(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder.getId(), result.get(0).getId());

        // Verify
        verify(orderRepository).findByUserOrderByOrderDateDesc(testUser);
    }

    @Test
    void getOrder_Success() {
        // Arrange
        when(orderRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testOrder));

        // Act
        Order result = orderService.getOrder(testUser, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());

        // Verify
        verify(orderRepository).findByIdAndUser(1L, testUser);
    }

    @Test
    void getAllOrders_Success() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findAll()).thenReturn(orders);

        // Act
        List<Order> result = orderService.getAllOrders();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder.getId(), result.get(0).getId());

        // Verify
        verify(orderRepository).findAll();
    }

    @Test
    void updateOrderStatus_Success() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        Order result = orderService.updateOrderStatus(1L, OrderStatus.DELIVERED);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.DELIVERED, result.getStatus());

        // Verify
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(testOrder);
    }

    @Test
    void updateOrderStatus_WithString_Success() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        Order result = orderService.updateOrderStatus(1L, "PENDING");

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());

        // Verify
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(testOrder);
    }
} 