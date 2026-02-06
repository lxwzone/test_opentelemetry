package com.example.tokenservice.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Executable;

@ConfigurationProperties("token.jwt")
public class TokenConfiguration {
    
    private long expiration = 3600;
    private String issuer = "token-service";
    private RsaConfiguration rsa = new RsaConfiguration();
    
    public long getExpiration() {
        return expiration;
    }
    
    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
    
    public String getIssuer() {
        return issuer;
    }
    
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    
    public RsaConfiguration getRsa() {
        return rsa;
    }
    
    public void setRsa(RsaConfiguration rsa) {
        this.rsa = rsa;
    }
    
    @ConfigurationProperties("rsa")
    public static class RsaConfiguration {
        private int keySize = 2048;
        private String privateKeyPath;
        private String publicKeyPath;
        
        public int getKeySize() {
            return keySize;
        }
        
        public void setKeySize(int keySize) {
            this.keySize = keySize;
        }
        
        public String getPrivateKeyPath() {
            return privateKeyPath;
        }
        
        public void setPrivateKeyPath(String privateKeyPath) {
            this.privateKeyPath = privateKeyPath;
        }
        
        public String getPublicKeyPath() {
            return publicKeyPath;
        }
        
        public void setPublicKeyPath(String publicKeyPath) {
            this.publicKeyPath = publicKeyPath;
        }
    }
}
