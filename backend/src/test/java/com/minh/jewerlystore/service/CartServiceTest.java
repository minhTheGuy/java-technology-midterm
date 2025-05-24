package com.minh.jewerlystore.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minh.jewerlystore.entity.Cart;
import com.minh.jewerlystore.entity.CartItem;
import com.minh.jewerlystore.entity.Product;
import com.minh.jewerlystore.entity.User;
import com.minh.jewerlystore.payload.response.CartResponse;
import com.minh.jewerlystore.repository.CartRepository;
import com.minh.jewerlystore.repository.ProductRepository;
import com.minh.jewerlystore.service.impl.CartServiceImpl;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private CartItem testCartItem;

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
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setImageUrl("test-image.jpg");

        // Setup test cart
        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUser(testUser);
        testCart.setItems(new ArrayList<>());

        // Setup test cart item
        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setCart(testCart);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(1);
    }

    @Test
    void getCart_ExistingCart_Success() {
        // Arrange
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        // Act
        CartResponse response = cartService.getCart(testUser);

        // Assert
        assertNotNull(response);
        assertEquals(testCart.getId(), response.getId());
        assertEquals(BigDecimal.ZERO, response.getTotalAmount());

        // Verify
        verify(cartRepository).findByUser(testUser);
    }

    @Test
    void getCart_NewCart_Success() {
        // Arrange
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        CartResponse response = cartService.getCart(testUser);

        // Assert
        assertNotNull(response);
        assertEquals(testCart.getId(), response.getId());
        assertEquals(BigDecimal.ZERO, response.getTotalAmount());

        // Verify
        verify(cartRepository).findByUser(testUser);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addToCart_NewItem_Success() {
        // Arrange
        testCart.getItems().clear();
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        CartResponse response = cartService.addToCart(testUser, 1L, 1);

        // Assert
        assertNotNull(response);
        assertEquals(testCart.getId(), response.getId());

        // Verify
        verify(cartRepository).findByUser(testUser);
        verify(productRepository).findById(1L);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addToCart_ExistingItem_Success() {
        // Arrange
        testCart.getItems().add(testCartItem);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        CartResponse response = cartService.addToCart(testUser, 1L, 1);

        // Assert
        assertNotNull(response);
        assertEquals(testCart.getId(), response.getId());

        // Verify
        verify(cartRepository).findByUser(testUser);
        verify(productRepository).findById(1L);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addToCart_ProductNotFound() {
        // Arrange
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> 
            cartService.addToCart(testUser, 1L, 1)
        );

        // Verify
        verify(cartRepository).findByUser(testUser);
        verify(productRepository).findById(1L);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void updateCartItemQuantity_Success() {
        // Arrange
        testCart.getItems().add(testCartItem);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        CartResponse response = cartService.updateCartItemQuantity(testUser, 1L, 2);

        // Assert
        assertNotNull(response);
        assertEquals(testCart.getId(), response.getId());

        // Verify
        verify(cartRepository).findByUser(testUser);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void updateCartItemQuantity_RemoveWhenZeroQuantity() {
        // Arrange
        testCart.getItems().add(testCartItem);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        CartResponse response = cartService.updateCartItemQuantity(testUser, 1L, 0);

        // Assert
        assertNotNull(response);
        assertEquals(testCart.getId(), response.getId());
        assertTrue(testCart.getItems().isEmpty());

        // Verify
        verify(cartRepository).findByUser(testUser);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void updateCartItemQuantity_CartNotFound() {
        // Arrange
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
            cartService.updateCartItemQuantity(testUser, 1L, 2)
        );

        // Verify
        verify(cartRepository).findByUser(testUser);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void removeFromCart_Success() {
        // Arrange
        testCart.getItems().add(testCartItem);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        CartResponse response = cartService.removeFromCart(testUser, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(testCart.getId(), response.getId());
        assertTrue(testCart.getItems().isEmpty());

        // Verify
        verify(cartRepository).findByUser(testUser);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void removeFromCart_ItemNotFound() {
        // Arrange
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
            cartService.removeFromCart(testUser, 1L)
        );

        // Verify
        verify(cartRepository).findByUser(testUser);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void clearCart_Success() {
        // Arrange
        testCart.getItems().add(testCartItem);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        cartService.clearCart(testUser);

        // Assert
        assertTrue(testCart.getItems().isEmpty());

        // Verify
        verify(cartRepository).findByUser(testUser);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void clearCart_CartNotFound() {
        // Arrange
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
            cartService.clearCart(testUser)
        );

        // Verify
        verify(cartRepository).findByUser(testUser);
        verify(cartRepository, never()).save(any(Cart.class));
    }
} 