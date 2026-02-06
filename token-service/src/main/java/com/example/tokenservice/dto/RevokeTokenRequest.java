package com.example.tokenservice.dto;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class RevokeTokenRequest {
    
    private String token;
    private String tokenTypeHint;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenTypeHint() {
        return tokenTypeHint;
    }

    public void setTokenTypeHint(String tokenTypeHint) {
        this.tokenTypeHint = tokenTypeHint;
    }
}
