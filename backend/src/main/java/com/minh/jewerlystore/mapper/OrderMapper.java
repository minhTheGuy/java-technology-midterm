package com.minh.jewerlystore.mapper;

import java.util.stream.Collectors;

import com.minh.jewerlystore.entity.Order;
import com.minh.jewerlystore.entity.OrderItem;
import com.minh.jewerlystore.payload.response.OrderResponse;
import com.minh.jewerlystore.payload.response.OrderResponse.OrderItemResponse;

public class OrderMapper {
    public static OrderResponse toOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUser().getId());
        response.setUsername(order.getUser().getUsername());
        response.setOrderDate(order.getOrderDate());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setShippingAddress(order.getShippingAddress());
        response.setShippingCity(order.getShippingCity());
        response.setShippingState(order.getShippingState());
        response.setShippingZip(order.getShippingZip());
        response.setShippingCountry(order.getShippingCountry());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setItems(order.getItems().stream()
                .map(OrderMapper::toOrderItemResponse)
                .collect(Collectors.toList()));
        return response;
    }

    private static OrderItemResponse toOrderItemResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setPrice(item.getPrice());
        response.setQuantity(item.getQuantity());
        response.setImageUrl(item.getProduct().getImageUrl());
        return response;
    }
} 