package com.minh.jewerlystore.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.minh.jewerlystore.entity.Order;
import com.minh.jewerlystore.entity.User;
import com.minh.jewerlystore.mapper.OrderMapper;
import com.minh.jewerlystore.payload.request.CheckoutRequest;
import com.minh.jewerlystore.payload.response.OrderResponse;
import com.minh.jewerlystore.repository.UserRepository;
import com.minh.jewerlystore.security.services.UserDetailsImpl;
import com.minh.jewerlystore.service.OrderService;
import com.minh.jewerlystore.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CheckoutRequest checkoutRequest) {
        User user = userService.getUserById(userDetails.getId());
        Order order = orderService.createOrder(user, checkoutRequest);
        return ResponseEntity.ok(OrderMapper.toOrderResponse(order));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getUserById(userDetails.getId());
        List<Order> orders = orderService.getUserOrders(user);
        List<OrderResponse> orderResponses = orders.stream()
                .map(OrderMapper::toOrderResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderResponses);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderResponse> getOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long orderId) {
        User user = userService.getUserById(userDetails.getId());
        Order order = orderService.getOrder(user, orderId);
        return ResponseEntity.ok(OrderMapper.toOrderResponse(order));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        List<OrderResponse> orderResponses = orders.stream()
                .map(OrderMapper::toOrderResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderResponses);
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        Order order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(OrderMapper.toOrderResponse(order));
    }
} 