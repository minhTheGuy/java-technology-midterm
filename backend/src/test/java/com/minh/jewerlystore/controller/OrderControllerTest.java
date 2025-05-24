package com.minh.jewerlystore.controller;

import com.minh.jewerlystore.entity.Order;
import com.minh.jewerlystore.entity.OrderStatus;
import com.minh.jewerlystore.entity.User;
import com.minh.jewerlystore.payload.request.CheckoutRequest;
import com.minh.jewerlystore.payload.response.OrderResponse;
import com.minh.jewerlystore.repository.UserRepository;
import com.minh.jewerlystore.security.services.UserDetailsImpl;
import com.minh.jewerlystore.service.OrderService;
import com.minh.jewerlystore.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderController orderController;

    private User testUser;
    private Order testOrder;
    private UserDetailsImpl userDetails;
    private CheckoutRequest checkoutRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        // Setup user details
        userDetails = new UserDetailsImpl(
            1L,
            "testuser",
            "test@example.com",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Setup test order
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setTotalAmount(new BigDecimal("100.00"));
        testOrder.setStatus(OrderStatus.PENDING);

        // Setup checkout request
        checkoutRequest = new CheckoutRequest();
        checkoutRequest.setShippingAddress("123 Test St");
        checkoutRequest.setShippingCity("Test City");
        checkoutRequest.setShippingState("Test State");
        checkoutRequest.setShippingZip("12345");
        checkoutRequest.setShippingCountry("Test Country");
        checkoutRequest.setPaymentMethod("CREDIT_CARD");
    }

    @Test
    void createOrder_Success() {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(testUser);
        when(orderService.createOrder(eq(testUser), any(CheckoutRequest.class))).thenReturn(testOrder);

        // Act
        ResponseEntity<OrderResponse> response = orderController.createOrder(userDetails, checkoutRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(testOrder.getId(), response.getBody().getId());
        
        // Verify
        verify(userService).getUserById(1L);
        verify(orderService).createOrder(eq(testUser), any(CheckoutRequest.class));
    }

    @Test
    void getUserOrders_Success() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(userService.getUserById(1L)).thenReturn(testUser);
        when(orderService.getUserOrders(testUser)).thenReturn(orders);

        // Act
        ResponseEntity<List<OrderResponse>> response = orderController.getUserOrders(userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testOrder.getId(), response.getBody().get(0).getId());

        // Verify
        verify(userService).getUserById(1L);
        verify(orderService).getUserOrders(testUser);
    }

    @Test
    void getOrder_Success() {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(testUser);
        when(orderService.getOrder(eq(testUser), eq(1L))).thenReturn(testOrder);

        // Act
        ResponseEntity<OrderResponse> response = orderController.getOrder(userDetails, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(testOrder.getId(), response.getBody().getId());

        // Verify
        verify(userService).getUserById(1L);
        verify(orderService).getOrder(testUser, 1L);
    }

    @Test
    void getAllOrders_Success() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderService.getAllOrders()).thenReturn(orders);

        // Act
        ResponseEntity<List<OrderResponse>> response = orderController.getAllOrders();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testOrder.getId(), response.getBody().get(0).getId());

        // Verify
        verify(orderService).getAllOrders();
    }

    @Test
    void updateOrderStatus_Success() {
        // Arrange
        String newStatus = "COMPLETED";
        when(orderService.updateOrderStatus(eq(1L), eq(newStatus))).thenReturn(testOrder);

        // Act
        ResponseEntity<OrderResponse> response = orderController.updateOrderStatus(1L, newStatus);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(testOrder.getId(), response.getBody().getId());

        // Verify
        verify(orderService).updateOrderStatus(1L, newStatus);
    }
} 