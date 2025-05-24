package com.minh.jewerlystore.payload.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private String imageUrl;
    private Integer quantity;

    // Constructor to create DTO from OrderItem entity
    public OrderItemDTO(com.minh.jewerlystore.entity.OrderItem orderItem) {
        this.id = orderItem.getId();
        this.productId = orderItem.getProduct().getId();
        this.productName = orderItem.getProduct().getName();
        this.price = orderItem.getPrice();
        this.imageUrl = orderItem.getProduct().getImageUrl();
        this.quantity = orderItem.getQuantity();
    }
} 