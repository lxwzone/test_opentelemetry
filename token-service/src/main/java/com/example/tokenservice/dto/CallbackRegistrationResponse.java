package com.example.tokenservice.dto;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class CallbackRegistrationResponse {
    
    private String clientId;
    private String callbackUrl;
    private String status;
    private String message;

    public CallbackRegistrationResponse() {
    }

    public CallbackRegistrationResponse(String clientId, String callbackUrl, String status, String message) {
        this.clientId = clientId;
        this.callbackUrl = callbackUrl;
        this.status = status;
        this.message = message;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
