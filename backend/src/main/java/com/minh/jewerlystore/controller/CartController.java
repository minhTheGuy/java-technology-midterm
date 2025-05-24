package com.minh.jewerlystore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minh.jewerlystore.entity.User;
import com.minh.jewerlystore.payload.response.CartResponse;
import com.minh.jewerlystore.security.services.UserDetailsImpl;
import com.minh.jewerlystore.service.CartService;
import com.minh.jewerlystore.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CartController {

    private final CartService cartService;
    
    private final UserService userService;

    @PostMapping("/add/{productId}")
    public ResponseEntity<CartResponse> addToCart(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        User user = userService.getUserById(userDetails.getId());
        CartResponse cart = cartService.addToCart(user, productId, quantity);
        return ResponseEntity.ok(cart);
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getUserById(userDetails.getId());
        CartResponse cartResponse = cartService.getCart(user);
        return ResponseEntity.ok(cartResponse);
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        User user = userService.getUserById(userDetails.getId());
        CartResponse cart = cartService.updateCartItemQuantity(user, productId, quantity);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long productId) {
        User user = userService.getUserById(userDetails.getId());
        CartResponse cart = cartService.removeFromCart(user, productId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getUserById(userDetails.getId());
        cartService.clearCart(user);
        return ResponseEntity.ok().build();
    }
} 