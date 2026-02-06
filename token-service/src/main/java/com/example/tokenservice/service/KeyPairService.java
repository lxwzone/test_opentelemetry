package com.example.tokenservice.service;

import com.example.tokenservice.config.TokenConfiguration;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Singleton
public class KeyPairService {
    
    private static final Logger LOG = LoggerFactory.getLogger(KeyPairService.class);
    
    private final KeyPair keyPair;
    private final String keyId;
    
    public KeyPairService(TokenConfiguration tokenConfiguration,
                         @Property(name = "token.jwt.rsa.key-size", defaultValue = "2048") int keySize) throws Exception {
        this.keyId = generateKeyId();
        
        String privateKeyPath = tokenConfiguration.getRsa().getPrivateKeyPath();
        String publicKeyPath = tokenConfiguration.getRsa().getPublicKeyPath();
        
        if (privateKeyPath != null && !privateKeyPath.isEmpty() && 
            publicKeyPath != null && !publicKeyPath.isEmpty()) {
            LOG.info("Loading RSA key pair from files");
            this.keyPair = loadKeyPairFromFile(privateKeyPath, publicKeyPath);
        } else {
            LOG.info("Generating new RSA key pair");
            this.keyPair = generateKeyPair(keySize);
        }
        
        LOG.info("RSA key pair initialized successfully with key ID: {}", keyId);
    }
    
    private KeyPair generateKeyPair(int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.generateKeyPair();
    }
    
    private KeyPair loadKeyPairFromFile(String privateKeyPath, String publicKeyPath) throws Exception {
        byte[] privateKeyBytes = java.nio.file.Files.readAllBytes(java.nio.file.Path.of(privateKeyPath));
        byte[] publicKeyBytes = java.nio.file.Files.readAllBytes(java.nio.file.Path.of(publicKeyPath));
        
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        
        return new KeyPair(publicKey, privateKey);
    }
    
    private String generateKeyId() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
    
    @NonNull
    public KeyPair getKeyPair() {
        return keyPair;
    }
    
    @NonNull
    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }
    
    @NonNull
    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }
    
    @NonNull
    public String getKeyId() {
        return keyId;
    }
    
    @NonNull
    public String getPublicKeyAsString() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }
}
