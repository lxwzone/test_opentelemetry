package com.example.dataclientservice.controller;

import com.example.dataclientservice.dto.TokenResponse;
import com.example.dataclientservice.service.TokenService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

@Controller
public class CallbackController {
    
    private static final Logger LOG = LoggerFactory.getLogger(CallbackController.class);
    private final TokenService tokenService;
    
    public CallbackController(TokenService tokenService) {
        this.tokenService = tokenService;
    }
    
    @Post("/callback")
    public HttpResponse<String> receiveToken(@Body TokenResponse tokenResponse) {
        try {
            LOG.info("Token received via callback: {}", tokenResponse.getAccessToken().substring(0, 50));
            LOG.info("Token type: {}", tokenResponse.getTokenType());
            LOG.info("Expires in: {} seconds", tokenResponse.getExpiresIn());
            LOG.info("Scope: {}", tokenResponse.getScope());
            
            // Store the token in the TokenService
            tokenService.storeTokenFromCallback(tokenResponse);
            
            return HttpResponse.ok("Token received successfully");
        } catch (Exception e) {
            LOG.error("Error processing token callback", e);
            return HttpResponse.serverError("Error processing token callback");
        }
    }
    
    @Get("/api/v1/token")
    public HttpResponse<Map<String, String>> getStoredToken() {
        try {
            String accessToken = tokenService.getAccessToken();
            LOG.info("Stored token retrieved: {}", accessToken.substring(0, 50));
            return HttpResponse.ok(Map.of("accessToken", accessToken));
        } catch (Exception e) {
            LOG.error("Error retrieving stored token", e);
            return HttpResponse.serverError();
        }
    }
}
