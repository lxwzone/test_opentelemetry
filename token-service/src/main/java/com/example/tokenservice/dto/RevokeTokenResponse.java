package com.example.tokenservice.dto;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class RevokeTokenResponse {
    
    private boolean revoked;
    private String message;

    public RevokeTokenResponse() {
    }

    public RevokeTokenResponse(boolean revoked, String message) {
        this.revoked = revoked;
        this.message = message;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
