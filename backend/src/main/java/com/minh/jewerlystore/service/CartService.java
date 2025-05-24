package com.minh.jewerlystore.service;

import com.minh.jewerlystore.entity.User;
import com.minh.jewerlystore.payload.response.CartResponse;

public interface CartService {
    CartResponse getCart(User user);
    CartResponse addToCart(User user, Long productId, Integer quantity);
    CartResponse updateCartItemQuantity(User user, Long productId, Integer quantity);
    CartResponse removeFromCart(User user, Long productId);
    void clearCart(User user);
} 