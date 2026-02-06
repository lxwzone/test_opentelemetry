package com.example.dataqueryservice.service;

import com.example.dataqueryservice.dto.Product;
import com.example.dataqueryservice.dto.User;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.MDC;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class MockDataServiceTest {

    @Inject
    private MockDataService mockDataService;

    @BeforeEach
    void setUp() {
        MDC.put("traceId", "test-trace-id");
    }

    @Test
    @DisplayName("Should get user by valid ID")
    void testGetUserById() {
        User user = mockDataService.getUserById(1L);

        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("john_doe", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
    }

    @Test
    @DisplayName("Should return null for non-existent user ID")
    void testGetUserByIdNotFound() {
        User user = mockDataService.getUserById(999L);

        assertNull(user);
    }

    @Test
    @DisplayName("Should get all users")
    void testGetAllUsers() {
        List<User> users = mockDataService.getAllUsers();

        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertEquals(3, users.size());
    }

    @Test
    @DisplayName("Should get product by valid ID")
    void testGetProductById() {
        Product product = mockDataService.getProductById(1L);

        assertNotNull(product);
        assertEquals(1L, product.getId());
        assertNotNull(product.getName());
        assertNotNull(product.getCategory());
    }

    @Test
    @DisplayName("Should return null for non-existent product ID")
    void testGetProductByIdNotFound() {
        Product product = mockDataService.getProductById(999L);

        assertNull(product);
    }

    @Test
    @DisplayName("Should get all products")
    void testGetAllProducts() {
        List<Product> products = mockDataService.getAllProducts();

        assertNotNull(products);
        assertFalse(products.isEmpty());
        assertTrue(products.size() >= 5);
    }

    @Test
    @DisplayName("Should get products by category")
    void testGetProductsByCategory() {
        List<Product> electronicsProducts = mockDataService.getProductsByCategory("Electronics");

        assertNotNull(electronicsProducts);
        assertFalse(electronicsProducts.isEmpty());
        assertTrue(electronicsProducts.size() >= 4);
        assertTrue(electronicsProducts.stream().allMatch(p -> p.getCategory().equals("Electronics")));
    }

    @Test
    @DisplayName("Should get all products when category is null")
    void testGetProductsByCategoryNull() {
        List<Product> products = mockDataService.getProductsByCategory(null);

        assertNotNull(products);
        assertTrue(products.size() >= 5);
    }

    @Test
    @DisplayName("Should get all products when category is empty")
    void testGetProductsByCategoryEmpty() {
        List<Product> products = mockDataService.getProductsByCategory("");

        assertNotNull(products);
        assertEquals(5, products.size());
    }

    @Test
    @DisplayName("Should create new product")
    void testCreateProduct() {
        Product product = mockDataService.createProduct(
                "New Product",
                "Test description",
                99.99,
                "Test Category",
                10
        );

        assertNotNull(product);
        assertEquals("New Product", product.getName());
        assertEquals("Test description", product.getDescription());
        assertEquals(99.99, product.getPrice());
        assertEquals("Test Category", product.getCategory());
        assertEquals(10, product.getStock());
        assertTrue(product.isAvailable());
    }

    @Test
    @DisplayName("Should update existing product")
    void testUpdateProduct() {
        Product updated = mockDataService.updateProduct(
                1L,
                "Updated Laptop",
                "Updated description",
                999.99,
                "Electronics",
                25
        );

        assertNotNull(updated);
        assertEquals("Updated Laptop", updated.getName());
        assertEquals("Updated description", updated.getDescription());
        assertEquals(999.99, updated.getPrice());
        assertEquals(25, updated.getStock());
    }

    @Test
    @DisplayName("Should return null when updating non-existent product")
    void testUpdateNonExistentProduct() {
        Product updated = mockDataService.updateProduct(
                999L,
                "Updated Name",
                "Updated description",
                99.99,
                "Category",
                10
        );

        assertNull(updated);
    }

    @Test
    @DisplayName("Should delete existing product")
    void testDeleteProduct() {
        boolean deleted = mockDataService.deleteProduct(1L);

        assertTrue(deleted);
        assertNull(mockDataService.getProductById(1L));
    }

    @Test
    @DisplayName("Should return false when deleting non-existent product")
    void testDeleteNonExistentProduct() {
        boolean deleted = mockDataService.deleteProduct(999L);

        assertFalse(deleted);
    }

    @Test
    @DisplayName("Should get correct product count")
    void testGetProductCount() {
        long count = mockDataService.getProductCount();

        assertEquals(5, count);
    }

    @Test
    @DisplayName("Should get correct user count")
    void testGetUserCount() {
        long count = mockDataService.getUserCount();

        assertEquals(3, count);
    }

    @Test
    @DisplayName("Should update product with partial data")
    void testUpdateProductPartial() {
        Product original = mockDataService.getProductById(1L);
        String originalName = original.getName();

        Product updated = mockDataService.updateProduct(
                1L,
                "New Name Only",
                null,
                null,
                null,
                null
        );

        assertNotNull(updated);
        assertEquals("New Name Only", updated.getName());
        assertEquals(original.getDescription(), updated.getDescription());
        assertEquals(original.getPrice(), updated.getPrice());
    }
}