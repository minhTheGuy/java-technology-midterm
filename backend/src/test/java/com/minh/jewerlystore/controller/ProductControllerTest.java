package com.minh.jewerlystore.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.minh.jewerlystore.entity.Product;
import com.minh.jewerlystore.service.ProductService;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Diamond Ring");
        testProduct.setDescription("Beautiful diamond ring");
        testProduct.setPrice(new BigDecimal("999.99"));
        testProduct.setCategory("Rings");
        testProduct.setBrand("Luxury Brand");
        testProduct.setColor("Silver");
        testProduct.setStockQuantity(10);
        testProduct.setImageUrl("ring.jpg");
    }

    @Test
    void getAllProducts_Success() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getAllProducts()).thenReturn(products);

        // Act
        ResponseEntity<List<Product>> response = productController.getAllProducts();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testProduct.getName(), response.getBody().get(0).getName());

        // Verify
        verify(productService).getAllProducts();
    }

    @Test
    void getProduct_Success() {
        // Arrange
        when(productService.getProductById(1L)).thenReturn(testProduct);

        // Act
        ResponseEntity<Product> response = productController.getProduct(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testProduct.getId(), response.getBody().getId());
        assertEquals(testProduct.getName(), response.getBody().getName());

        // Verify
        verify(productService).getProductById(1L);
    }

    @Test
    void getProduct_NotFound() {
        // Arrange
        when(productService.getProductById(1L)).thenThrow(new EntityNotFoundException("Product not found"));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
            productController.getProduct(1L)
        );

        // Verify
        verify(productService).getProductById(1L);
    }

    @Test
    void searchProducts_Success() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        when(productService.searchProducts(
            "Rings",
            new BigDecimal("500.00"),
            new BigDecimal("1000.00"),
            "Luxury Brand",
            "Silver"
        )).thenReturn(products);

        // Act
        ResponseEntity<List<Product>> response = productController.searchProducts(
            "Rings",
            new BigDecimal("500.00"),
            new BigDecimal("1000.00"),
            "Luxury Brand",
            "Silver"
        );

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testProduct.getName(), response.getBody().get(0).getName());

        // Verify
        verify(productService).searchProducts(
            "Rings",
            new BigDecimal("500.00"),
            new BigDecimal("1000.00"),
            "Luxury Brand",
            "Silver"
        );
    }

    @Test
    void searchByName_Success() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        when(productService.searchByName("Diamond")).thenReturn(products);

        // Act
        ResponseEntity<List<Product>> response = productController.searchByName("Diamond");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testProduct.getName(), response.getBody().get(0).getName());

        // Verify
        verify(productService).searchByName("Diamond");
    }

    @Test
    void createProduct_Success() {
        // Arrange
        when(productService.createProduct(any(Product.class))).thenReturn(testProduct);

        // Act
        ResponseEntity<Product> response = productController.createProduct(testProduct);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testProduct.getName(), response.getBody().getName());
        assertEquals(testProduct.getPrice(), response.getBody().getPrice());

        // Verify
        verify(productService).createProduct(testProduct);
    }

    @Test
    void updateProduct_Success() {
        // Arrange
        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Ring");
        updatedProduct.setPrice(new BigDecimal("1099.99"));
        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(updatedProduct);

        // Act
        ResponseEntity<Product> response = productController.updateProduct(1L, updatedProduct);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedProduct.getName(), response.getBody().getName());
        assertEquals(updatedProduct.getPrice(), response.getBody().getPrice());

        // Verify
        verify(productService).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    void updateProduct_NotFound() {
        // Arrange
        when(productService.updateProduct(eq(1L), any(Product.class)))
            .thenThrow(new EntityNotFoundException("Product not found"));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
            productController.updateProduct(1L, testProduct)
        );

        // Verify
        verify(productService).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    void deleteProduct_Success() {
        // Act
        ResponseEntity<Void> response = productController.deleteProduct(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify
        verify(productService).deleteProduct(1L);
    }
} 