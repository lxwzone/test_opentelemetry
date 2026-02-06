package com.example.dataqueryservice.controller;

import com.example.dataqueryservice.dto.CreateProductRequest;
import com.example.dataqueryservice.dto.PagedResponse;
import com.example.dataqueryservice.dto.Product;
import com.example.dataqueryservice.service.MockDataService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@Controller("/api/v1/products")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {
    
    private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);
    
    private final MockDataService mockDataService;
    
    public ProductController(MockDataService mockDataService) {
        this.mockDataService = mockDataService;
    }
    
    @Get
    @Operation(summary = "Get all products", description = "Retrieve a list of all products")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved products")
    public HttpResponse<PagedResponse<Product>> getAllProducts(
            @Parameter(description = "Page number (0-based)") @QueryValue(defaultValue = "0") int page,
            @Parameter(description = "Page size") @QueryValue(defaultValue = "10") int size,
            @Parameter(description = "Filter by category") @QueryValue String category) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            List<Product> products;
            if (category != null && !category.isEmpty()) {
                products = mockDataService.getProductsByCategory(category);
            } else {
                products = mockDataService.getAllProducts();
            }
            
            int start = page * size;
            int end = Math.min(start + size, products.size());
            
            List<Product> pagedProducts = products.subList(start, end);
            PagedResponse<Product> response = new PagedResponse<>(
                    pagedProducts, page, size, products.size()
            );
            
            LOG.info("Retrieved {} products, page: {}, size: {}, category: {}, traceId: {}", 
                    pagedProducts.size(), page, size, category, traceId);
            return HttpResponse.ok(response);
        } catch (Exception e) {
            LOG.error("Error retrieving products, traceId: {}", traceId, e);
            return HttpResponse.serverError();
        } finally {
            MDC.remove("traceId");
        }
    }
    
    @Get("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved product")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public HttpResponse<Product> getProductById(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            Product product = mockDataService.getProductById(id);
            if (product != null) {
                LOG.info("Retrieved product: {}, traceId: {}", id, traceId);
                return HttpResponse.ok(product);
            } else {
                LOG.warn("Product not found: {}, traceId: {}", id, traceId);
                return HttpResponse.notFound();
            }
        } catch (Exception e) {
            LOG.error("Error retrieving product: {}, traceId: {}", id, traceId, e);
            return HttpResponse.serverError();
        } finally {
            MDC.remove("traceId");
        }
    }
    
    @Post
    @Operation(summary = "Create a new product", description = "Create a new product in the system")
    @ApiResponse(responseCode = "201", description = "Product created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @Secured({"ROLE_ADMIN"})
    public HttpResponse<Product> createProduct(@Valid @Body CreateProductRequest request) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            Product product = mockDataService.createProduct(
                    request.getName(),
                    request.getDescription(),
                    request.getPrice(),
                    request.getCategory(),
                    request.getStock()
            );
            
            LOG.info("Created product: {}, traceId: {}", product.getName(), traceId);
            return HttpResponse.created(product);
        } catch (Exception e) {
            LOG.error("Error creating product, traceId: {}", traceId, e);
            return HttpResponse.serverError();
        } finally {
            MDC.remove("traceId");
        }
    }
    
    @Delete("/{id}")
    @Operation(summary = "Delete a product", description = "Delete a product by its ID")
    @ApiResponse(responseCode = "204", description = "Product deleted successfully")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @Secured({"ROLE_ADMIN"})
    public HttpResponse<Void> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            boolean deleted = mockDataService.deleteProduct(id);
            if (deleted) {
                LOG.info("Deleted product: {}, traceId: {}", id, traceId);
                return HttpResponse.noContent();
            } else {
                LOG.warn("Product not found for deletion: {}, traceId: {}", id, traceId);
                return HttpResponse.notFound();
            }
        } catch (Exception e) {
            LOG.error("Error deleting product: {}, traceId: {}", id, traceId, e);
            return HttpResponse.serverError();
        } finally {
            MDC.remove("traceId");
        }
    }
}
