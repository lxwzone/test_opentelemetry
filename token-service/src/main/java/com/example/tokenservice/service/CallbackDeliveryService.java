package com.example.tokenservice.service;

import com.example.tokenservice.dto.TokenResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.context.annotation.Property;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Singleton
public class CallbackDeliveryService {
    
    private static final Logger LOG = LoggerFactory.getLogger(CallbackDeliveryService.class);
    
    private final ExecutorService callbackExecutor;
    private final HttpClient httpClient;
    private final int maxAttempts;
    private final long backoffMs;
    
    public CallbackDeliveryService(@Client HttpClient httpClient,
                                  @Property(name = "token.callback.retry.max-attempts", defaultValue = "3") int maxAttempts,
                                  @Property(name = "token.callback.retry.backoff", defaultValue = "1000") long backoffMs) {
        this.httpClient = httpClient;
        this.maxAttempts = maxAttempts;
        this.backoffMs = backoffMs;
        this.callbackExecutor = Executors.newFixedThreadPool(5);
        LOG.info("Callback delivery service initialized with max attempts: {}, backoff: {}ms", 
                maxAttempts, backoffMs);
    }
    
    public void deliverTokenAsync(String callbackUrl, TokenResponse tokenResponse, String traceId) {
        CompletableFuture.runAsync(() -> {
            try {
                deliverTokenWithRetry(callbackUrl, tokenResponse, traceId);
            } catch (Exception e) {
                LOG.error("Failed to deliver token to callback URL: {}, traceId: {}", 
                        callbackUrl, traceId, e);
            }
        }, callbackExecutor);
    }
    
    private void deliverTokenWithRetry(String callbackUrl, TokenResponse tokenResponse, String traceId) {
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < maxAttempts) {
            attempt++;
            try {
                MDC.put("traceId", traceId);
                
                URI uri = UriBuilder.of(callbackUrl).build();
                HttpRequest<TokenResponse> request = HttpRequest.POST(uri, tokenResponse);
                
                HttpResponse<String> response = httpClient.toBlocking().exchange(request, String.class);
                
                if (response.getStatus().getCode() >= 200 && response.getStatus().getCode() < 300) {
                    LOG.info("Token delivered successfully to callback URL: {} on attempt: {}, traceId: {}", 
                            callbackUrl, attempt, traceId);
                    return;
                } else {
                    LOG.warn("Callback returned status: {} for URL: {}, attempt: {}, traceId: {}", 
                            response.getStatus().getCode(), callbackUrl, attempt, traceId);
                }
            } catch (Exception e) {
                lastException = e;
                LOG.warn("Failed to deliver token to callback URL: {} on attempt: {}, traceId: {}", 
                        callbackUrl, attempt, traceId, e);
                
                if (attempt < maxAttempts) {
                    try {
                        long delay = backoffMs * (long) Math.pow(2, attempt - 1);
                        LOG.debug("Waiting {}ms before retry, traceId: {}", delay, traceId);
                        TimeUnit.MILLISECONDS.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } finally {
                MDC.remove("traceId");
            }
        }
        
        LOG.error("Failed to deliver token after {} attempts to URL: {}, traceId: {}", 
                maxAttempts, callbackUrl, traceId, lastException);
    }
    
    public void shutdown() {
        if (callbackExecutor != null && !callbackExecutor.isShutdown()) {
            callbackExecutor.shutdown();
            try {
                if (!callbackExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    callbackExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                callbackExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            LOG.info("Callback delivery service shutdown");
        }
    }
}
