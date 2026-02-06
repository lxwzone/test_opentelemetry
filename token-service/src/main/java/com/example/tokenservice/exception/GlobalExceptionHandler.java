package com.example.tokenservice.exception;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Produces
@Singleton
@Requires(classes = {Exception.class, ExceptionHandler.class})
public class GlobalExceptionHandler implements ExceptionHandler<Exception, HttpResponse<?>> {
    
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @Override
    public HttpResponse<?> handle(HttpRequest request, Exception exception) {
        LOG.error("Exception occurred while processing request: {}", request.getPath(), exception);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("status", 500);
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", exception.getMessage());
        errorResponse.put("path", request.getPath());
        
        return HttpResponse.serverError(errorResponse);
    }
}
