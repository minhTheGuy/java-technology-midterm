package com.minh.jewerlystore.payload.request;

import lombok.Data;

@Data
public class CheckoutRequest {
    private String shippingAddress;
    private String shippingCity;
    private String shippingState;
    private String shippingZip;
    private String shippingCountry;
    private String paymentMethod;
} 