package com.minh.jewerlystore.service;

import java.util.List;

import com.minh.jewerlystore.entity.Order;
import com.minh.jewerlystore.entity.OrderStatus;
import com.minh.jewerlystore.entity.User;
import com.minh.jewerlystore.payload.request.CheckoutRequest;

public interface OrderService {
    Order createOrder(User user, CheckoutRequest checkoutRequest);
    List<Order> getUserOrders(User user);
    Order getOrder(User user, Long orderId);
    List<Order> getAllOrders();
    Order updateOrderStatus(Long orderId, OrderStatus status);
    Order updateOrderStatus(Long orderId, String status);
}