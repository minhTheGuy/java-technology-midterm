package com.minh.jewerlystore.payload.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.minh.jewerlystore.entity.OrderStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderResponse {
    private Long id;
    private Long userId;
    private String username;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String shippingAddress;
    private String shippingCity;
    private String shippingState;
    private String shippingZip;
    private String shippingCountry;
    private String paymentMethod;
    private String paymentStatus;
    private List<OrderItemResponse> items;

    @Data
    @NoArgsConstructor
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private BigDecimal price;
        private Integer quantity;
        private String imageUrl;
    }
} 