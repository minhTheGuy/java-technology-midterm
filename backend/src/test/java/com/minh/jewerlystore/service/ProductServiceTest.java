package com.minh.jewerlystore.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minh.jewerlystore.entity.Product;
import com.minh.jewerlystore.repository.ProductRepository;
import com.minh.jewerlystore.service.impl.ProductServiceImpl;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

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
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProduct.getName(), result.get(0).getName());

        // Verify
        verify(productRepository).findAll();
    }

    @Test
    void getProductById_Success() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        Product result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        assertEquals(testProduct.getName(), result.getName());

        // Verify
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_NotFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
            productService.getProductById(1L)
        );

        // Verify
        verify(productRepository).findById(1L);
    }

    @Test
    void searchProducts_Success() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByFilters(
            "Rings",
            new BigDecimal("500.00"),
            new BigDecimal("1000.00"),
            "Luxury Brand",
            "Silver"
        )).thenReturn(products);

        // Act
        List<Product> result = productService.searchProducts(
            "Rings",
            new BigDecimal("500.00"),
            new BigDecimal("1000.00"),
            "Luxury Brand",
            "Silver"
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProduct.getName(), result.get(0).getName());

        // Verify
        verify(productRepository).findByFilters(
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
        when(productRepository.findByNameContainingIgnoreCase("Diamond")).thenReturn(products);

        // Act
        List<Product> result = productService.searchByName("Diamond");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProduct.getName(), result.get(0).getName());

        // Verify
        verify(productRepository).findByNameContainingIgnoreCase("Diamond");
    }

    @Test
    void createProduct_Success() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.createProduct(testProduct);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getName(), result.getName());
        assertEquals(testProduct.getPrice(), result.getPrice());

        // Verify
        verify(productRepository).save(testProduct);
    }

    @Test
    void updateProduct_Success() {
        // Arrange
        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Ring");
        updatedProduct.setPrice(new BigDecimal("1099.99"));
        updatedProduct.setCategory("Rings");
        updatedProduct.setBrand("Luxury Brand");
        updatedProduct.setColor("Gold");
        updatedProduct.setStockQuantity(5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // Act
        Product result = productService.updateProduct(1L, updatedProduct);

        // Assert
        assertNotNull(result);
        assertEquals(updatedProduct.getName(), result.getName());
        assertEquals(updatedProduct.getPrice(), result.getPrice());
        assertEquals(updatedProduct.getColor(), result.getColor());

        // Verify
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_NotFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
            productService.updateProduct(1L, testProduct)
        );

        // Verify
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_Success() {
        // Act
        productService.deleteProduct(1L);

        // Verify
        verify(productRepository).deleteById(1L);
    }
} 