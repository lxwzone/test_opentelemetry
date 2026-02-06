package com.example.tokenservice.dto;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class PublicKeyResponse {
    
    private String publicKey;
    private String algorithm = "RS256";
    private String keyId;

    public PublicKeyResponse() {
    }

    public PublicKeyResponse(String publicKey, String keyId) {
        this.publicKey = publicKey;
        this.keyId = keyId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }
}
