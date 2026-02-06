package com.example.dataclientservice.controller;

import com.example.dataclientservice.dto.PagedResponse;
import com.example.dataclientservice.dto.User;
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

import java.util.Map;
import java.util.UUID;

@Controller("/api/v1/users")
@Tag(name = "Client Users", description = "Client-side user endpoints with circuit breaker and retry")
public class ClientUserController {
    
    private static final Logger LOG = LoggerFactory.getLogger(ClientUserController.class);
    
    private final DataQueryServiceClient dataQueryServiceClient;
    
    public ClientUserController(DataQueryServiceClient dataQueryServiceClient) {
        this.dataQueryServiceClient = dataQueryServiceClient;
    }
    
    @Get
    @Operation(summary = "Get all users (with circuit breaker)", description = "Retrieve a list of all users with circuit breaker protection")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved users")
    @ApiResponse(responseCode = "503", description = "Service unavailable (circuit breaker open)")
    public HttpResponse<PagedResponse<User>> getAllUsers(
            @Parameter(description = "Page number (0-based)") @QueryValue(defaultValue = "0") int page,
            @Parameter(description = "Page size") @QueryValue(defaultValue = "10") int size) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            PagedResponse<User> users = dataQueryServiceClient.getAllUsers(page, size);
            LOG.info("Retrieved {} users via client, page: {}, size: {}, traceId: {}", 
                    users.getData().size(), page, size, traceId);
            return HttpResponse.ok(users);
        } catch (Exception e) {
            LOG.error("Error retrieving users via client, traceId: {}", traceId, e);
            throw e;
        } finally {
            MDC.remove("traceId");
        }
    }
    
    @Get("/{id}")
    @Operation(summary = "Get user by ID (with circuit breaker)", description = "Retrieve a specific user by their ID with circuit breaker protection")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "503", description = "Service unavailable (circuit breaker open)")
    public HttpResponse<User> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            User user = dataQueryServiceClient.getUserById(id);
            if (user != null) {
                LOG.info("Retrieved user {} via client, traceId: {}", id, traceId);
                return HttpResponse.ok(user);
            } else {
                LOG.warn("User {} not found via client, traceId: {}", id, traceId);
                return HttpResponse.notFound();
            }
        } catch (Exception e) {
            LOG.error("Error retrieving user {} via client, traceId: {}", id, traceId, e);
            throw e;
        } finally {
            MDC.remove("traceId");
        }
    }
}
