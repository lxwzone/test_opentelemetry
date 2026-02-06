package com.example.dataqueryservice.controller;

import com.example.dataqueryservice.dto.PagedResponse;
import com.example.dataqueryservice.dto.User;
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

import java.util.List;
import java.util.UUID;

@Controller("/api/v1/users")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Tag(name = "Users", description = "User management endpoints")
public class UserController {
    
    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);
    
    private final MockDataService mockDataService;
    
    public UserController(MockDataService mockDataService) {
        this.mockDataService = mockDataService;
    }
    
    @Get
    @Operation(summary = "Get all users", description = "Retrieve a list of all users")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved users")
    public HttpResponse<PagedResponse<User>> getAllUsers(
            @Parameter(description = "Page number (0-based)") @QueryValue(defaultValue = "0") int page,
            @Parameter(description = "Page size") @QueryValue(defaultValue = "10") int size) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            List<User> users = mockDataService.getAllUsers();
            int start = page * size;
            int end = Math.min(start + size, users.size());
            
            List<User> pagedUsers = users.subList(start, end);
            PagedResponse<User> response = new PagedResponse<>(
                    pagedUsers, page, size, users.size()
            );
            
            LOG.info("Retrieved {} users, page: {}, size: {}, traceId: {}", pagedUsers.size(), page, size, traceId);
            return HttpResponse.ok(response);
        } catch (Exception e) {
            LOG.error("Error retrieving users, traceId: {}", traceId, e);
            return HttpResponse.serverError();
        } finally {
            MDC.remove("traceId");
        }
    }
    
    @Get("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user")
    @ApiResponse(responseCode = "404", description = "User not found")
    public HttpResponse<User> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            User user = mockDataService.getUserById(id);
            if (user != null) {
                LOG.info("Retrieved user: {}, traceId: {}", id, traceId);
                return HttpResponse.ok(user);
            } else {
                LOG.warn("User not found: {}, traceId: {}", id, traceId);
                return HttpResponse.notFound();
            }
        } catch (Exception e) {
            LOG.error("Error retrieving user: {}, traceId: {}", id, traceId, e);
            return HttpResponse.serverError();
        } finally {
            MDC.remove("traceId");
        }
    }
}
