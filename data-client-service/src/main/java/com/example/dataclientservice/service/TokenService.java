package com.example.dataclientservice.service;

import com.example.dataclientservice.dto.TokenRequest;
import com.example.dataclientservice.dto.TokenResponse;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Singleton
public class TokenService {
    
    private static final Logger LOG = LoggerFactory.getLogger(TokenService.class);
    
    private final HttpClient tokenServiceClient;
    private final String clientId;
    private final String clientSecret;
    private final String scope;
    private final long refreshBeforeExpiration;
    
    private final ConcurrentHashMap<String, TokenInfo> tokenCache = new ConcurrentHashMap<>();
    private final ReentrantLock tokenLock = new ReentrantLock();
    
    public TokenService(@Client("${client.token.service.url}") HttpClient tokenServiceClient,
                       @Property(name = "client.token.service.client-id") String clientId,
                       @Property(name = "client.token.service.client-secret") String clientSecret,
                       @Property(name = "client.token.service.scope") String scope,
                       @Property(name = "client.token.service.refresh-before-expiration") long refreshBeforeExpiration) {
        this.tokenServiceClient = tokenServiceClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.refreshBeforeExpiration = refreshBeforeExpiration;
        LOG.info("Token service initialized for client: {}", clientId);
    }
    
    public String getAccessToken() {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            TokenInfo tokenInfo = tokenCache.get(clientId);
            
            if (tokenInfo != null && !isTokenExpired(tokenInfo)) {
                LOG.debug("Using cached token for client: {}, traceId: {}", clientId, traceId);
                return tokenInfo.getAccessToken();
            }
            
            tokenLock.lock();
            try {
                tokenInfo = tokenCache.get(clientId);
                
                if (tokenInfo != null && !isTokenExpired(tokenInfo)) {
                    LOG.debug("Using cached token after lock for client: {}, traceId: {}", clientId, traceId);
                    return tokenInfo.getAccessToken();
                }
                
                TokenResponse tokenResponse = fetchNewToken(traceId);
                if (tokenResponse != null) {
                    long expirationTime = System.currentTimeMillis() + (tokenResponse.getExpiresIn() * 1000);
                    tokenInfo = new TokenInfo(tokenResponse.getAccessToken(), expirationTime);
                    tokenCache.put(clientId, tokenInfo);
                    LOG.info("New token acquired for client: {}, expires at: {}, traceId: {}", 
                            clientId, expirationTime, traceId);
                    return tokenResponse.getAccessToken();
                }
            } finally {
                tokenLock.unlock();
            }
            
            throw new RuntimeException("Failed to acquire access token");
        } catch (Exception e) {
            LOG.error("Error getting access token for client: {}, traceId: {}", clientId, traceId, e);
            throw new RuntimeException("Failed to acquire access token", e);
        } finally {
            MDC.remove("traceId");
        }
    }
    
    private boolean isTokenExpired(TokenInfo tokenInfo) {
        long currentTime = System.currentTimeMillis();
        long expirationTime = tokenInfo.getExpirationTime();
        long refreshThreshold = expirationTime - (refreshBeforeExpiration * 1000);
        return currentTime >= refreshThreshold;
    }
    
    private TokenResponse fetchNewToken(String traceId) {
        try {
            TokenRequest tokenRequest = new TokenRequest(clientId, clientSecret, "client_credentials", scope);
            
            HttpRequest<TokenRequest> request = HttpRequest.POST("/oauth/token", tokenRequest);
            HttpResponse<TokenResponse> response = tokenServiceClient.toBlocking().exchange(request, TokenResponse.class);
            
            if (response.getStatus().getCode() >= 200 && response.getStatus().getCode() < 300) {
                LOG.info("Successfully fetched new token from token service, traceId: {}", traceId);
                return response.body();
            } else {
                LOG.error("Failed to fetch token, status: {}, traceId: {}", response.getStatus().getCode(), traceId);
                return null;
            }
        } catch (Exception e) {
            LOG.error("Exception while fetching token, traceId: {}", traceId, e);
            return null;
        }
    }
    
    public void invalidateToken() {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        
        try {
            tokenCache.remove(clientId);
            LOG.info("Token invalidated for client: {}, traceId: {}", clientId, traceId);
        } finally {
            MDC.remove("traceId");
        }
    }
    
    public static class TokenInfo {
        private final String accessToken;
        private final long expirationTime;
        
        public TokenInfo(String accessToken, long expirationTime) {
            this.accessToken = accessToken;
            this.expirationTime = expirationTime;
        }
        
        public String getAccessToken() {
            return accessToken;
        }
        
        public long getExpirationTime() {
            return expirationTime;
        }
    }
}
