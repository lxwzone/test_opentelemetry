package com.example.dataqueryservice.service;

import com.example.dataqueryservice.dto.Product;
import com.example.dataqueryservice.dto.User;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class MockDataService {
    
    private static final Logger LOG = LoggerFactory.getLogger(MockDataService.class);
    
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    
    public MockDataService() {
        initializeMockData();
        LOG.info("Mock data service initialized with {} users and {} products", 
                users.size(), products.size());
    }
    
    private void initializeMockData() {
        users.put(1L, new User(1L, "john_doe", "john@example.com", "John", "Doe", "USER", true, 
                Arrays.asList("read", "write")));
        users.put(2L, new User(2L, "jane_smith", "jane@example.com", "Jane", "Smith", "ADMIN", true, 
                Arrays.asList("read", "write", "delete")));
        users.put(3L, new User(3L, "bob_wilson", "bob@example.com", "Bob", "Wilson", "USER", true, 
                Arrays.asList("read")));
        
        products.put(1L, new Product(1L, "Laptop", "High-performance laptop", 1299.99, "Electronics", 50, true));
        products.put(2L, new Product(2L, "Wireless Mouse", "Ergonomic wireless mouse", 29.99, "Electronics", 200, true));
        products.put(3L, new Product(3L, "Mechanical Keyboard", "RGB mechanical keyboard", 149.99, "Electronics", 100, true));
        products.put(4L, new Product(4L, "Monitor 27\"", "4K IPS monitor", 399.99, "Electronics", 75, true));
        products.put(5L, new Product(5L, "USB-C Hub", "7-in-1 USB-C hub", 49.99, "Accessories", 150, true));
    }
    
    public User getUserById(Long id) {
        String traceId = MDC.get("traceId");
        User user = users.get(id);
        LOG.debug("Retrieved user by id: {}, found: {}, traceId: {}", id, user != null, traceId);
        return user;
    }
    
    public List<User> getAllUsers() {
        String traceId = MDC.get("traceId");
        List<User> userList = new ArrayList<>(users.values());
        LOG.debug("Retrieved all users, count: {}, traceId: {}", userList.size(), traceId);
        return userList;
    }
    
    public Product getProductById(Long id) {
        String traceId = MDC.get("traceId");
        Product product = products.get(id);
        LOG.debug("Retrieved product by id: {}, found: {}, traceId: {}", id, product != null, traceId);
        return product;
    }
    
    public List<Product> getAllProducts() {
        String traceId = MDC.get("traceId");
        List<Product> productList = new ArrayList<>(products.values());
        LOG.debug("Retrieved all products, count: {}, traceId: {}", productList.size(), traceId);
        return productList;
    }
    
    public List<Product> getProductsByCategory(String category) {
        String traceId = MDC.get("traceId");
        List<Product> filteredProducts = products.values().stream()
                .filter(p -> category == null || category.isEmpty() || p.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
        LOG.debug("Retrieved products by category: {}, count: {}, traceId: {}", category, filteredProducts.size(), traceId);
        return filteredProducts;
    }
    
    public Product createProduct(String name, String description, Double price, String category, Integer stock) {
        String traceId = MDC.get("traceId");
        Long id = (long) (products.size() + 1);
        Product product = new Product(id, name, description, price, category, stock, true);
        products.put(id, product);
        LOG.info("Created product: {}, id: {}, traceId: {}", name, id, traceId);
        return product;
    }
    
    public Product updateProduct(Long id, String name, String description, Double price, String category, Integer stock) {
        String traceId = MDC.get("traceId");
        Product product = products.get(id);
        if (product != null) {
            if (name != null) product.setName(name);
            if (description != null) product.setDescription(description);
            if (price != null) product.setPrice(price);
            if (category != null) product.setCategory(category);
            if (stock != null) product.setStock(stock);
            LOG.info("Updated product: {}, id: {}, traceId: {}", name, id, traceId);
        }
        return product;
    }
    
    public boolean deleteProduct(Long id) {
        String traceId = MDC.get("traceId");
        Product removed = products.remove(id);
        boolean deleted = removed != null;
        LOG.info("Deleted product: {}, id: {}, traceId: {}", deleted, id, traceId);
        return deleted;
    }
    
    public long getProductCount() {
        return products.size();
    }
    
    public long getUserCount() {
        return users.size();
    }
}
