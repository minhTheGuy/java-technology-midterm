package com.minh.jewerlystore.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minh.jewerlystore.entity.Order;
import com.minh.jewerlystore.entity.OrderItem;
import com.minh.jewerlystore.entity.OrderStatus;
import com.minh.jewerlystore.entity.Product;
import com.minh.jewerlystore.entity.User;
import com.minh.jewerlystore.exception.InsufficientStockException;
import com.minh.jewerlystore.payload.request.CheckoutRequest;
import com.minh.jewerlystore.payload.response.CartResponse;
import com.minh.jewerlystore.repository.OrderRepository;
import com.minh.jewerlystore.repository.ProductRepository;
import com.minh.jewerlystore.service.CartService;
import com.minh.jewerlystore.service.OrderService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductRepository productRepository;

    @Override
    public Order createOrder(User user, CheckoutRequest checkoutRequest) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null when creating order");
        }
        
        CartResponse cart = cartService.getCart(user);
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Validate stock for all items before processing
        for (CartResponse.CartItemResponse cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + cartItem.getProductId()));
            
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                    String.format("Insufficient stock for product '%s'. Available: %d, Requested: %d",
                        product.getName(), product.getStockQuantity(), cartItem.getQuantity())
                );
            }
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(checkoutRequest.getShippingAddress());
        order.setShippingCity(checkoutRequest.getShippingCity());
        order.setShippingState(checkoutRequest.getShippingState());
        order.setShippingZip(checkoutRequest.getShippingZip());
        order.setShippingCountry(checkoutRequest.getShippingCountry());
        order.setPaymentMethod(checkoutRequest.getPaymentMethod());
        order.setPaymentStatus("PENDING");

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartResponse.CartItemResponse cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + cartItem.getProductId()));
            
            // Update product stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProductPrice());
            order.getItems().add(orderItem);
            
            totalAmount = totalAmount.add(cartItem.getProductPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(user);
        
        return savedOrder;
    }

    @Override
    public List<Order> getUserOrders(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null when getting orders");
        }
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    @Override
    public Order getOrder(User user, Long orderId) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null when getting order");
        }
        return orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Override
    public Order updateOrderStatus(Long orderId, String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return updateOrderStatus(orderId, orderStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status + 
                ". Valid statuses are: " + String.join(", ", 
                java.util.Arrays.stream(OrderStatus.values())
                    .map(OrderStatus::name)
                    .toList()));
        }
    }
} 