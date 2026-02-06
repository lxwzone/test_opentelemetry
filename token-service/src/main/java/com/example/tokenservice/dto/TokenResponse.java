package com.example.tokenservice.dto;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class TokenResponse {
    
    private String accessToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private String scope;
    private String issuedAt;

    public TokenResponse() {
    }

    public TokenResponse(String accessToken, Long expiresIn, String scope, String issuedAt) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.scope = scope;
        this.issuedAt = issuedAt;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(String issuedAt) {
        this.issuedAt = issuedAt;
    }
}
