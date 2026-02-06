package com.example.dataclientservice.controller;

import com.example.dataclientservice.dto.PagedResponse;
import com.example.dataclientservice.dto.Product;
import com.example.dataclientservice.service.DataQueryServiceClient;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

@Controller("/api/v1/products")
@Tag(name = "Client Products", description = "Client-side product endpoints with circuit breaker and retry")
public class ClientProductController {
    
    private static final Logger LOG = LoggerFactory.getLogger(ClientProductController.class);
    
    private final DataQueryServiceClient dataQueryServiceClient;
    
    public ClientProductController(DataQueryServiceClient dataQueryServiceClient) {
        this.dataQueryServiceClient = dataQueryServiceClient;
    }
    
    @Get
    @Operation(summary = "Get all products (with circuit breaker)", description = "Retrieve a list of all products with circuit breaker protection")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved products")
    @ApiResponse(responseCode = "503", description = "Service unavailable (circuit breaker open)")
    public HttpResponse<PagedResponse<Product>> getAllProducts(
            @Parameter(description = "Page number (0-based)") @QueryValue(defaultValue = "0") int page,
            @Parameter(description = "Page size") @QueryValue(defaultValue = "10") int size,
            @Parameter(description = "Filter by category") @QueryValue String category) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            PagedResponse<Product> products = dataQueryServiceClient.getAllProducts(page, size, category);
            LOG.info("Retrieved {} products via client, page: {}, size: {}, category: {}, traceId: {}", 
                    products.getData().size(), page, size, category, traceId);
            return HttpResponse.ok(products);
        } catch (Exception e) {
            LOG.error("Error retrieving products via client, traceId: {}", traceId, e);
            throw e;
        } finally {
            MDC.remove("traceId");
        }
    }
    
    @Get("/{id}")
    @Operation(summary = "Get product by ID (with circuit breaker)", description = "Retrieve a specific product by its ID with circuit breaker protection")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved product")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @ApiResponse(responseCode = "503", description = "Service unavailable (circuit breaker open)")
    public HttpResponse<Product> getProductById(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            Product product = dataQueryServiceClient.getProductById(id);
            if (product != null) {
                LOG.info("Retrieved product {} via client, traceId: {}", id, traceId);
                return HttpResponse.ok(product);
            } else {
                LOG.warn("Product {} not found via client, traceId: {}", id, traceId);
                return HttpResponse.notFound();
            }
        } catch (Exception e) {
            LOG.error("Error retrieving product {} via client, traceId: {}", id, traceId, e);
            throw e;
        } finally {
            MDC.remove("traceId");
        }
    }
}
