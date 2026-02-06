package com.example.dataclientservice.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Controller("/health")
public class HealthController {
    
    private static final Logger LOG = LoggerFactory.getLogger(HealthController.class);
    
    @Get
    public Map<String, Object> health() {
        LOG.debug("Health check requested");
        return Map.of(
                "status", "UP",
                "service", "data-client-service",
                "timestamp", System.currentTimeMillis()
        );
    }
    
    @Get("/detailed")
    public Map<String, Object> detailedHealth() {
        LOG.debug("Detailed health check requested");
        return Map.of(
                "status", "UP",
                "service", "data-client-service",
                "timestamp", System.currentTimeMillis(),
                "details", "All systems operational"
        );
    }
}
