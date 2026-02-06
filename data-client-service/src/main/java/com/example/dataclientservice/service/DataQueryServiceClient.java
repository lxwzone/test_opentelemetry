package com.example.dataclientservice.service;

import com.example.dataclientservice.dto.PagedResponse;
import com.example.dataclientservice.dto.Product;
import com.example.dataclientservice.dto.User;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.retry.annotation.Retryable;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

@Singleton
public class DataQueryServiceClient {
    
    private static final Logger LOG = LoggerFactory.getLogger(DataQueryServiceClient.class);
    
    private final HttpClient dataQueryServiceClient;
    private final TokenService tokenService;
    
    public DataQueryServiceClient(@Client("${client.data-query.service.url}") HttpClient dataQueryServiceClient,
                                  TokenService tokenService) {
        this.dataQueryServiceClient = dataQueryServiceClient;
        this.tokenService = tokenService;
        LOG.info("Data query service client initialized");
    }
    
    @Retryable(
        attempts = "${client.retry.max-attempts:3}",
        delay = "${client.retry.initial-delay:1s}",
        multiplier = "${client.retry.multiplier:2.0}",
        maxDelay = "${client.retry.max-delay:10s}"
    )
    public PagedResponse<User> getAllUsers(int page, int size) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            String accessToken = tokenService.getAccessToken();
            
            HttpRequest<?> request = HttpRequest.GET("/api/v1/users?page=" + page + "&size=" + size)
                    .header("Authorization", "Bearer " + accessToken);
            
            HttpResponse<PagedResponse<User>> response = dataQueryServiceClient.toBlocking()
                    .exchange(request);
            
            if (response.getStatus().getCode() >= 200 && response.getStatus().getCode() < 300) {
                LOG.info("Successfully retrieved users, page: {}, size: {}, traceId: {}", page, size, traceId);
                return response.body();
            } else {
                LOG.error("Failed to retrieve users, status: {}, traceId: {}", response.getStatus().getCode(), traceId);
                throw new RuntimeException("Failed to retrieve users");
            }
        } catch (Exception e) {
            LOG.error("Exception while retrieving users, traceId: {}", traceId, e);
            throw new RuntimeException("Failed to retrieve users", e);
        } finally {
            MDC.remove("traceId");
        }
    }
    
    @Retryable(
        attempts = "${client.retry.max-attempts:3}",
        delay = "${client.retry.initial-delay:1s}",
        multiplier = "${client.retry.multiplier:2.0}",
        maxDelay = "${client.retry.max-delay:10s}"
    )
    public User getUserById(Long id) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            String accessToken = tokenService.getAccessToken();
            
            HttpRequest<?> request = HttpRequest.GET("/api/v1/users/" + id)
                    .header("Authorization", "Bearer " + accessToken);
            
            HttpResponse<User> response = dataQueryServiceClient.toBlocking()
                    .exchange(request);
            
            if (response.getStatus().getCode() >= 200 && response.getStatus().getCode() < 300) {
                LOG.info("Successfully retrieved user: {}, traceId: {}", id, traceId);
                return response.body();
            } else if (response.getStatus().getCode() == 404) {
                LOG.warn("User not found: {}, traceId: {}", id, traceId);
                return null;
            } else {
                LOG.error("Failed to retrieve user, status: {}, traceId: {}", response.getStatus().getCode(), traceId);
                throw new RuntimeException("Failed to retrieve user");
            }
        } catch (Exception e) {
            LOG.error("Exception while retrieving user: {}, traceId: {}", id, traceId, e);
            throw new RuntimeException("Failed to retrieve user", e);
        } finally {
            MDC.remove("traceId");
        }
    }
    
    @Retryable(
        attempts = "${client.retry.max-attempts:3}",
        delay = "${client.retry.initial-delay:1s}",
        multiplier = "${client.retry.multiplier:2.0}",
        maxDelay = "${client.retry.max-delay:10s}"
    )
    public PagedResponse<Product> getAllProducts(int page, int size, String category) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            String accessToken = tokenService.getAccessToken();
            
            String url = "/api/v1/products?page=" + page + "&size=" + size;
            if (category != null && !category.isEmpty()) {
                url += "&category=" + category;
            }
            
            HttpRequest<?> request = HttpRequest.GET(url)
                    .header("Authorization", "Bearer " + accessToken);
            
            HttpResponse<PagedResponse<Product>> response = dataQueryServiceClient.toBlocking()
                    .exchange(request);
            
            if (response.getStatus().getCode() >= 200 && response.getStatus().getCode() < 300) {
                LOG.info("Successfully retrieved products, page: {}, size: {}, category: {}, traceId: {}", 
                        page, size, category, traceId);
                return response.body();
            } else {
                LOG.error("Failed to retrieve products, status: {}, traceId: {}", response.getStatus().getCode(), traceId);
                throw new RuntimeException("Failed to retrieve products");
            }
        } catch (Exception e) {
            LOG.error("Exception while retrieving products, traceId: {}", traceId, e);
            throw new RuntimeException("Failed to retrieve products", e);
        } finally {
            MDC.remove("traceId");
        }
    }
    
    @Retryable(
        attempts = "${client.retry.max-attempts:3}",
        delay = "${client.retry.initial-delay:1s}",
        multiplier = "${client.retry.multiplier:2.0}",
        maxDelay = "${client.retry.max-delay:10s}"
    )
    public Product getProductById(Long id) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            String accessToken = tokenService.getAccessToken();
            
            HttpRequest<?> request = HttpRequest.GET("/api/v1/products/" + id)
                    .header("Authorization", "Bearer " + accessToken);
            
            HttpResponse<Product> response = dataQueryServiceClient.toBlocking()
                    .exchange(request);
            
            if (response.getStatus().getCode() >= 200 && response.getStatus().getCode() < 300) {
                LOG.info("Successfully retrieved product: {}, traceId: {}", id, traceId);
                return response.body();
            } else if (response.getStatus().getCode() == 404) {
                LOG.warn("Product not found: {}, traceId: {}", id, traceId);
                return null;
            } else {
                LOG.error("Failed to retrieve product, status: {}, traceId: {}", response.getStatus().getCode(), traceId);
                throw new RuntimeException("Failed to retrieve product");
            }
        } catch (Exception e) {
            LOG.error("Exception while retrieving product: {}, traceId: {}", id, traceId, e);
            throw new RuntimeException("Failed to retrieve product", e);
        } finally {
            MDC.remove("traceId");
        }
    }
}