package com.minh.jewerlystore.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.minh.jewerlystore.entity.Product;

public interface ProductService {
    List<Product> getAllProducts();
    Product getProductById(Long id);
    List<Product> searchProducts(String category, BigDecimal minPrice, BigDecimal maxPrice, String brand, String color);
    List<Product> searchByName(String name);
    Product createProduct(Product product);
    Product updateProduct(Long id, Product product);
    void deleteProduct(Long id);
}