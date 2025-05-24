package com.minh.jewerlystore.payload.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private String imageUrl;
    private Integer quantity;

    // Constructor to create DTO from CartItem entity
    public CartItemDTO(com.minh.jewerlystore.entity.CartItem cartItem) {
        this.id = cartItem.getId();
        this.productId = cartItem.getProduct().getId();
        this.productName = cartItem.getProduct().getName();
        this.productPrice = cartItem.getProduct().getPrice();
        this.imageUrl = cartItem.getProduct().getImageUrl();
        this.quantity = cartItem.getQuantity();
    }
} 