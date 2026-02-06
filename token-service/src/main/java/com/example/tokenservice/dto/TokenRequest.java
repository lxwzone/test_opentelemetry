package com.example.tokenservice.dto;

import io.micronaut.core.annotation.Introspected;
import jakarta.validation.constraints.NotBlank;

@Introspected
public class TokenRequest {
    
    @NotBlank(message = "Client ID is required")
    private String clientId;
    
    @NotBlank(message = "Client secret is required")
    private String clientSecret;
    
    private String scope;
    
    private String grantType = "client_credentials";

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }
}
