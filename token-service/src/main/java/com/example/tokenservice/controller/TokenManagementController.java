package com.example.tokenservice.controller;

import com.example.tokenservice.dto.PublicKeyResponse;
import com.example.tokenservice.dto.RevokeTokenRequest;
import com.example.tokenservice.dto.RevokeTokenResponse;
import com.example.tokenservice.service.JwtTokenService;
import com.example.tokenservice.service.KeyPairService;
import com.example.tokenservice.service.TokenBlacklistService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

@Controller("/api/v1")
@Validated
public class TokenManagementController {
    
    private static final Logger LOG = LoggerFactory.getLogger(TokenManagementController.class);
    
    private final KeyPairService keyPairService;
    private final JwtTokenService jwtTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    
    public TokenManagementController(KeyPairService keyPairService,
                                     JwtTokenService jwtTokenService,
                                     TokenBlacklistService tokenBlacklistService) {
        this.keyPairService = keyPairService;
        this.jwtTokenService = jwtTokenService;
        this.tokenBlacklistService = tokenBlacklistService;
    }
    
    @Get("/public-key")
    public HttpResponse<PublicKeyResponse> getPublicKey() {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            String publicKey = keyPairService.getPublicKeyAsString();
            String keyId = keyPairService.getKeyId();
            
            PublicKeyResponse response = new PublicKeyResponse(publicKey, keyId);
            
            LOG.info("Public key requested, keyId: {}, traceId: {}", keyId, traceId);
            return HttpResponse.ok(response);
        } catch (Exception e) {
            LOG.error("Error retrieving public key, traceId: {}", traceId, e);
            return HttpResponse.serverError();
        } finally {
            MDC.remove("traceId");
        }
    }
    
    @Post("/revoke")
    public HttpResponse<RevokeTokenResponse> revokeToken(@Valid @Body RevokeTokenRequest request) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            String token = request.getToken();
            
            if (token == null || token.isEmpty()) {
                LOG.warn("Revoke token request with empty token, traceId: {}", traceId);
                return HttpResponse.badRequest(new RevokeTokenResponse(false, "Token is required"));
            }
            
            if (tokenBlacklistService.isTokenRevoked(token)) {
                LOG.warn("Token already revoked, traceId: {}", traceId);
                return HttpResponse.ok(new RevokeTokenResponse(true, "Token already revoked"));
            }
            
            tokenBlacklistService.revokeToken(token, "Manual revocation via API");
            
            LOG.info("Token revoked successfully, traceId: {}", traceId);
            return HttpResponse.ok(new RevokeTokenResponse(true, "Token revoked successfully"));
        } catch (Exception e) {
            LOG.error("Error revoking token, traceId: {}", traceId, e);
            return HttpResponse.serverError(new RevokeTokenResponse(false, e.getMessage()));
        } finally {
            MDC.remove("traceId");
        }
    }
    
    @Get("/validate")
    public HttpResponse<Boolean> validateToken(@QueryValue("token") String token) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            if (token == null || token.isEmpty()) {
                LOG.warn("Validate token request with empty token, traceId: {}", traceId);
                return HttpResponse.badRequest();
            }
            
            if (tokenBlacklistService.isTokenRevoked(token)) {
                LOG.info("Token is revoked, traceId: {}", traceId);
                return HttpResponse.ok(false);
            }
            
            if (jwtTokenService.isTokenExpired(token)) {
                LOG.info("Token is expired, traceId: {}", traceId);
                return HttpResponse.ok(false);
            }
            
            String subject = jwtTokenService.getSubjectFromToken(token);
            LOG.info("Token validated successfully for subject: {}, traceId: {}", subject, traceId);
            return HttpResponse.ok(true);
        } catch (Exception e) {
            LOG.error("Error validating token, traceId: {}", traceId, e);
            return HttpResponse.ok(false);
        } finally {
            MDC.remove("traceId");
        }
    }
    
    @Get("/blacklist/count")
    public HttpResponse<Integer> getBlacklistCount() {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            int count = tokenBlacklistService.getRevokedTokenCount();
            LOG.info("Blacklist count requested: {}, traceId: {}", count, traceId);
            return HttpResponse.ok(count);
        } finally {
            MDC.remove("traceId");
        }
    }
}
