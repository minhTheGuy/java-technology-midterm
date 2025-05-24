package com.minh.jewerlystore.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.minh.jewerlystore.entity.User;
import com.minh.jewerlystore.payload.response.CartResponse;
import com.minh.jewerlystore.security.services.UserDetailsImpl;
import com.minh.jewerlystore.service.CartService;
import com.minh.jewerlystore.service.UserService;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CartController cartController;

    private UserDetailsImpl userDetails;
    private User testUser;
    private CartResponse testCartResponse;

    @BeforeEach
    void setUp() {
        // Setup user details
        userDetails = new UserDetailsImpl(
            1L,
            "testuser",
            "test@example.com",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        // Setup cart response
        testCartResponse = new CartResponse();
        testCartResponse.setId(1L);
        testCartResponse.setItems(new ArrayList<>());
        testCartResponse.setTotalAmount(BigDecimal.ZERO);

        // Setup common mock
        when(userService.getUserById(1L)).thenReturn(testUser);
    }

    @Test
    void addToCart_Success() {
        // Arrange
        when(cartService.addToCart(eq(testUser), eq(1L), eq(1)))
            .thenReturn(testCartResponse);

        // Act
        ResponseEntity<CartResponse> response = cartController.addToCart(userDetails, 1L, 1);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testCartResponse, response.getBody());

        // Verify
        verify(userService).getUserById(1L);
        verify(cartService).addToCart(testUser, 1L, 1);
    }

    @Test
    void getCart_Success() {
        // Arrange
        when(cartService.getCart(testUser)).thenReturn(testCartResponse);

        // Act
        ResponseEntity<CartResponse> response = cartController.getCart(userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testCartResponse, response.getBody());

        // Verify
        verify(userService).getUserById(1L);
        verify(cartService).getCart(testUser);
    }

    @Test
    void updateCartItem_Success() {
        // Arrange
        when(cartService.updateCartItemQuantity(eq(testUser), eq(1L), eq(2)))
            .thenReturn(testCartResponse);

        // Act
        ResponseEntity<CartResponse> response = cartController.updateCartItem(userDetails, 1L, 2);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testCartResponse, response.getBody());

        // Verify
        verify(userService).getUserById(1L);
        verify(cartService).updateCartItemQuantity(testUser, 1L, 2);
    }

    @Test
    void removeFromCart_Success() {
        // Arrange
        when(cartService.removeFromCart(eq(testUser), eq(1L)))
            .thenReturn(testCartResponse);

        // Act
        ResponseEntity<CartResponse> response = cartController.removeFromCart(userDetails, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testCartResponse, response.getBody());

        // Verify
        verify(userService).getUserById(1L);
        verify(cartService).removeFromCart(testUser, 1L);
    }

    @Test
    void clearCart_Success() {
        // Act
        ResponseEntity<Void> response = cartController.clearCart(userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify
        verify(userService).getUserById(1L);
        verify(cartService).clearCart(testUser);
    }
} 