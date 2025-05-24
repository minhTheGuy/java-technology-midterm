package com.minh.jewerlystore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.minh.jewerlystore.entity.Product;
import com.minh.jewerlystore.service.FileStorageService;
import com.minh.jewerlystore.service.ProductService;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final FileStorageService fileStorageService;

    private final ProductService productService;

    @PostMapping("/upload/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadImage(@PathVariable Long productId, @RequestParam("file") MultipartFile file) {
        Product product = productService.getProductById(productId);
        
        // Delete old image if exists
        if (product.getImageUrl() != null) {
            fileStorageService.deleteFile(product.getImageUrl());
        }

        // Store new image
        String fileName = fileStorageService.storeFile(file);
        product.setImageUrl(fileName);
        productService.updateProduct(productId, product);

        return ResponseEntity.ok().body("File uploaded successfully: " + fileName);
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        Resource file = fileStorageService.loadFileAsResource(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // You might want to determine this dynamically based on file extension
                .body(file);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteImage(@PathVariable Long productId) {
        Product product = productService.getProductById(productId);
        
        if (product.getImageUrl() != null) {
            fileStorageService.deleteFile(product.getImageUrl());
            product.setImageUrl(null);
            productService.updateProduct(productId, product);
            return ResponseEntity.ok().body("Image deleted successfully");
        }
        
        return ResponseEntity.notFound().build();
    }
} 