package com.example.tokenservice.service;

import com.example.tokenservice.dto.TokenResponse;
import io.micronaut.context.annotation.Property;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ClientService {
    
    private static final Logger LOG = LoggerFactory.getLogger(ClientService.class);
    
    private final Map<String, ClientInfo> clients = new ConcurrentHashMap<>();
    private final Map<String, String> callbackUrls = new ConcurrentHashMap<>();
    
    @Property(name = "pre.registered.client.id", defaultValue = "data-client-service")
    private String preRegisteredClientId;
    
    @Property(name = "pre.registered.client.secret", defaultValue = "secret123")
    private String preRegisteredClientSecret;
    
    @Property(name = "pre.registered.callback.url", defaultValue = "http://localhost:8082/callback")
    private String preRegisteredCallbackUrl;
    
    public ClientService() {
        initializePreRegisteredClient();
    }
    
    private void initializePreRegisteredClient() {
        ClientInfo clientInfo = new ClientInfo(
                preRegisteredClientId,
                preRegisteredClientSecret,
                preRegisteredCallbackUrl,
                new String[]{"read", "write"}
        );
        clients.put(preRegisteredClientId, clientInfo);
        callbackUrls.put(preRegisteredClientId, preRegisteredCallbackUrl);
        LOG.info("Pre-registered client initialized: {}", preRegisteredClientId);
    }
    
    public boolean validateClient(String clientId, String clientSecret) {
        ClientInfo client = clients.get(clientId);
        if (client == null) {
            LOG.warn("Client not found: {}", clientId);
            return false;
        }
        boolean isValid = client.getClientSecret().equals(clientSecret);
        if (!isValid) {
            LOG.warn("Invalid client secret for client: {}", clientId);
        }
        return isValid;
    }
    
    public void registerCallback(String clientId, String callbackUrl, String[] scopes) {
        ClientInfo client = clients.get(clientId);
        if (client == null) {
            throw new IllegalArgumentException("Client not found: " + clientId);
        }
        
        client.setCallbackUrl(callbackUrl);
        if (scopes != null && scopes.length > 0) {
            client.setScopes(scopes);
        }
        callbackUrls.put(clientId, callbackUrl);
        
        LOG.info("Callback registered for client: {} at URL: {}", clientId, callbackUrl);
    }
    
    public String getCallbackUrl(String clientId) {
        return callbackUrls.get(clientId);
    }
    
    public ClientInfo getClient(String clientId) {
        return clients.get(clientId);
    }
    
    public Collection<ClientInfo> getAllClients() {
        return clients.values();
    }
    
    public static class ClientInfo {
        private final String clientId;
        private final String clientSecret;
        private String callbackUrl;
        private String[] scopes;
        private final Date createdAt;
        
        public ClientInfo(String clientId, String clientSecret, String callbackUrl, String[] scopes) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.callbackUrl = callbackUrl;
            this.scopes = scopes;
            this.createdAt = new Date();
        }
        
        public String getClientId() {
            return clientId;
        }
        
        public String getClientSecret() {
            return clientSecret;
        }
        
        public String getCallbackUrl() {
            return callbackUrl;
        }
        
        public void setCallbackUrl(String callbackUrl) {
            this.callbackUrl = callbackUrl;
        }
        
        public String[] getScopes() {
            return scopes;
        }
        
        public void setScopes(String[] scopes) {
            this.scopes = scopes;
        }
        
        public Date getCreatedAt() {
            return createdAt;
        }
    }
}
