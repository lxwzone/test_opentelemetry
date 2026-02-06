package com.example.tokenservice.controller;

import com.example.tokenservice.dto.CallbackRegistrationRequest;
import com.example.tokenservice.dto.CallbackRegistrationResponse;
import com.example.tokenservice.dto.TokenRequest;
import com.example.tokenservice.dto.TokenResponse;
import com.example.tokenservice.service.CallbackDeliveryService;
import com.example.tokenservice.service.ClientService;
import com.example.tokenservice.service.JwtTokenService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

@Controller("/oauth")
@Validated
public class TokenController {
    
    private static final Logger LOG = LoggerFactory.getLogger(TokenController.class);
    
    private final JwtTokenService jwtTokenService;
    private final ClientService clientService;
    private final CallbackDeliveryService callbackDeliveryService;
    
    public TokenController(JwtTokenService jwtTokenService,
                          ClientService clientService,
                          CallbackDeliveryService callbackDeliveryService) {
        this.jwtTokenService = jwtTokenService;
        this.clientService = clientService;
        this.callbackDeliveryService = callbackDeliveryService;
    }
    
    @Post("/token")
    public HttpResponse<TokenResponse> issueToken(@Valid @Body TokenRequest request) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            LOG.info("Token request received from client: {}", request.getClientId());
            
            if (!clientService.validateClient(request.getClientId(), request.getClientSecret())) {
                LOG.warn("Invalid client credentials for client: {}", request.getClientId());
                return HttpResponse.unauthorized();
            }
            
            TokenResponse tokenResponse = jwtTokenService.generateToken(request, traceId);
            
            String callbackUrl = clientService.getCallbackUrl(request.getClientId());
            if (callbackUrl != null && !callbackUrl.isEmpty()) {
                callbackDeliveryService.deliverTokenAsync(callbackUrl, tokenResponse, traceId);
            }
            
            LOG.info("Token issued successfully for client: {}, traceId: {}", request.getClientId(), traceId);
            return HttpResponse.ok(tokenResponse);
        } catch (Exception e) {
            LOG.error("Error issuing token for client: {}, traceId: {}", request.getClientId(), traceId, e);
            return HttpResponse.serverError();
        } finally {
            MDC.remove("traceId");
        }
    }
    
    @Post("/register-callback")
    public HttpResponse<CallbackRegistrationResponse> registerCallback(@Valid @Body CallbackRegistrationRequest request) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            LOG.info("Callback registration request from client: {}", request.getClientId());
            
            if (!clientService.validateClient(request.getClientId(), request.getClientSecret())) {
                LOG.warn("Invalid client credentials for callback registration: {}", request.getClientId());
                return HttpResponse.unauthorized();
            }
            
            clientService.registerCallback(request.getClientId(), request.getCallbackUrl(), request.getScopes());
            
            CallbackRegistrationResponse response = new CallbackRegistrationResponse(
                    request.getClientId(),
                    request.getCallbackUrl(),
                    "success",
                    "Callback registered successfully"
            );
            
            LOG.info("Callback registered successfully for client: {}, URL: {}, traceId: {}", 
                    request.getClientId(), request.getCallbackUrl(), traceId);
            return HttpResponse.ok(response);
        } catch (Exception e) {
            LOG.error("Error registering callback for client: {}, traceId: {}", 
                    request.getClientId(), traceId, e);
            CallbackRegistrationResponse response = new CallbackRegistrationResponse(
                    request.getClientId(),
                    request.getCallbackUrl(),
                    "error",
                    e.getMessage()
            );
            return HttpResponse.serverError(response);
        } finally {
            MDC.remove("traceId");
        }
    }
}
