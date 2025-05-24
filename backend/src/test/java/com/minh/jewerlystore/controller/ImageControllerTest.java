package com.minh.jewerlystore.controller;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import com.minh.jewerlystore.entity.Product;
import com.minh.jewerlystore.service.FileStorageService;
import com.minh.jewerlystore.service.ProductService;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ImageController imageController;

    private Product testProduct;
    private MockMultipartFile testFile;
    private Resource testResource;

    @BeforeEach
    void setUp() throws MalformedURLException {
        // Setup test product
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Diamond Ring");
        testProduct.setDescription("Beautiful diamond ring");
        testProduct.setPrice(new BigDecimal("999.99"));
        testProduct.setCategory("Rings");
        testProduct.setImageUrl("old-image.jpg");

        // Setup test multipart file
        testFile = new MockMultipartFile(
            "file",
            "test-image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        // Setup test resource
        Path path = Paths.get("test-image.jpg");
        testResource = new UrlResource(path.toUri());
    }

    @Test
    void uploadImage_Success() {
        // Arrange
        when(productService.getProductById(1L)).thenReturn(testProduct);
        when(fileStorageService.storeFile(any())).thenReturn("new-image.jpg");
        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(testProduct);

        // Act
        ResponseEntity<?> response = imageController.uploadImage(1L, testFile);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("File uploaded successfully: new-image.jpg", response.getBody());

        // Verify
        verify(productService).getProductById(1L);
        verify(fileStorageService).storeFile(testFile);
        verify(fileStorageService).deleteFile("old-image.jpg");
        verify(productService).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    void uploadImage_ProductNotFound() {
        // Arrange
        when(productService.getProductById(1L))
            .thenThrow(new EntityNotFoundException("Product not found"));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
            imageController.uploadImage(1L, testFile)
        );

        // Verify
        verify(productService).getProductById(1L);
        verify(fileStorageService, never()).storeFile(any());
        verify(fileStorageService, never()).deleteFile(any());
        verify(productService, never()).updateProduct(any(), any());
    }

    @Test
    void getImage_Success() {
        // Arrange
        when(fileStorageService.loadFileAsResource("test-image.jpg")).thenReturn(testResource);

        // Act
        ResponseEntity<Resource> response = imageController.getImage("test-image.jpg");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
        assertEquals(testResource, response.getBody());

        // Verify
        verify(fileStorageService).loadFileAsResource("test-image.jpg");
    }

    @Test
    void deleteImage_Success() {
        // Arrange
        when(productService.getProductById(1L)).thenReturn(testProduct);
        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(testProduct);

        // Act
        ResponseEntity<?> response = imageController.deleteImage(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Image deleted successfully", response.getBody());

        // Verify
        verify(productService).getProductById(1L);
        verify(fileStorageService).deleteFile("old-image.jpg");
        verify(productService).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    void deleteImage_ProductNotFound() {
        // Arrange
        when(productService.getProductById(1L))
            .thenThrow(new EntityNotFoundException("Product not found"));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
            imageController.deleteImage(1L)
        );

        // Verify
        verify(productService).getProductById(1L);
        verify(fileStorageService, never()).deleteFile(any());
        verify(productService, never()).updateProduct(any(), any());
    }

    @Test
    void deleteImage_NoImageUrl() {
        // Arrange
        testProduct.setImageUrl(null);
        when(productService.getProductById(1L)).thenReturn(testProduct);

        // Act
        ResponseEntity<?> response = imageController.deleteImage(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        // Verify
        verify(productService).getProductById(1L);
        verify(fileStorageService, never()).deleteFile(any());
        verify(productService, never()).updateProduct(any(), any());
    }
} 