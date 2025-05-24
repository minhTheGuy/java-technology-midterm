package com.minh.jewerlystore.service.impl;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minh.jewerlystore.entity.Cart;
import com.minh.jewerlystore.entity.CartItem;
import com.minh.jewerlystore.entity.Product;
import com.minh.jewerlystore.entity.User;
import com.minh.jewerlystore.payload.response.CartResponse;
import com.minh.jewerlystore.payload.response.CartResponse.CartItemResponse;
import com.minh.jewerlystore.repository.CartRepository;
import com.minh.jewerlystore.repository.ProductRepository;
import com.minh.jewerlystore.service.CartService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Override
    public CartResponse getCart(User user) {
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> createNewCart(user));
        return convertToCartResponse(cart);
    }

    @Override
    public CartResponse addToCart(User user, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> createNewCart(user));
        Product product = getProductById(productId);

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        }

        cart = cartRepository.save(cart);
        return convertToCartResponse(cart);
    }

    @Override
    public CartResponse updateCartItemQuantity(User user, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Product not found in cart"));

        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }

        cart = cartRepository.save(cart);
        return convertToCartResponse(cart);
    }

    @Override
    public CartResponse removeFromCart(User user, Long productId) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for user"));
        
        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Product not found in user's cart"));

        cart.getItems().remove(itemToRemove);
        cart = cartRepository.save(cart);
        return convertToCartResponse(cart);
    }

    @Override
    public void clearCart(User user) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart createNewCart(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null when creating cart");
        }
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));
    }

    private CartResponse convertToCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setItems(cart.getItems().stream()
                .map(this::convertToCartItemResponse)
                .collect(Collectors.toList()));
        response.setTotalAmount(cart.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return response;
    }

    private CartItemResponse convertToCartItemResponse(CartItem item) {
        CartItemResponse response = new CartItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setProductPrice(item.getProduct().getPrice());
        response.setQuantity(item.getQuantity());
        response.setImageUrl(item.getProduct().getImageUrl());
        return response;
    }
} 