package com.example.tokenservice.service;

import io.micronaut.context.annotation.Property;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class TokenBlacklistService {
    
    private static final Logger LOG = LoggerFactory.getLogger(TokenBlacklistService.class);
    
    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor;
    private final long cleanupInterval;
    
    public TokenBlacklistService(@Property(name = "token.blacklist.cleanup-interval", defaultValue = "3600") long cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        startCleanupTask();
        LOG.info("Token blacklist service initialized with cleanup interval: {} seconds", cleanupInterval);
    }
    
    public void revokeToken(String token, String reason) {
        long revokedAt = System.currentTimeMillis();
        blacklistedTokens.put(token, revokedAt);
        LOG.info("Token revoked at: {}, reason: {}", revokedAt, reason);
    }
    
    public boolean isTokenRevoked(String token) {
        return blacklistedTokens.containsKey(token);
    }
    
    public long getRevocationTime(String token) {
        return blacklistedTokens.getOrDefault(token, 0L);
    }
    
    public int getRevokedTokenCount() {
        return blacklistedTokens.size();
    }
    
    private void startCleanupTask() {
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredTokens, 
                cleanupInterval, cleanupInterval, TimeUnit.SECONDS);
    }
    
    private void cleanupExpiredTokens() {
        try {
            long now = System.currentTimeMillis();
            long expirationThreshold = now - (3600 * 1000);
            
            int beforeSize = blacklistedTokens.size();
            blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() < expirationThreshold);
            int afterSize = blacklistedTokens.size();
            
            if (beforeSize > afterSize) {
                LOG.info("Cleaned up {} expired revoked tokens", beforeSize - afterSize);
            }
        } catch (Exception e) {
            LOG.error("Error during token blacklist cleanup", e);
        }
    }
    
    public void shutdown() {
        if (cleanupExecutor != null && !cleanupExecutor.isShutdown()) {
            cleanupExecutor.shutdown();
            LOG.info("Token blacklist service shutdown");
        }
    }
}
