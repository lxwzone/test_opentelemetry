package com.example.tokenservice.service;

import com.example.tokenservice.config.TokenConfiguration;
import com.example.tokenservice.dto.TokenRequest;
import com.example.tokenservice.dto.TokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.micronaut.context.annotation.Property;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class JwtTokenService {
    
    private static final Logger LOG = LoggerFactory.getLogger(JwtTokenService.class);
    
    private final TokenConfiguration tokenConfiguration;
    private final KeyPairService keyPairService;
    private final String secret;
    
    public JwtTokenService(TokenConfiguration tokenConfiguration,
                          KeyPairService keyPairService,
                          @Property(name = "micronaut.security.token.jwt.signatures.secret.generator.secret", defaultValue = "pleaseChangeThisSecretForASecretKeyForJWTTokenGeneration") String secret) {
        this.tokenConfiguration = tokenConfiguration;
        this.keyPairService = keyPairService;
        this.secret = secret;
    }
    
    public TokenResponse generateToken(TokenRequest request, String traceId) {
        try {
            String clientId = request.getClientId();
            String scope = request.getScope() != null ? request.getScope() : "default";
            
            long expirationTime = System.currentTimeMillis() + (tokenConfiguration.getExpiration() * 1000);
            long issuedAt = System.currentTimeMillis();
            
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", clientId);
            claims.put("aud", "data-client-service");
            claims.put("scope", scope);
            claims.put("client_id", clientId);
            claims.put("trace_id", traceId);
            
            Key signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(clientId)
                    .setIssuer(tokenConfiguration.getIssuer())
                    .setIssuedAt(new Date(issuedAt))
                    .setExpiration(new Date(expirationTime))
                    .setId(keyPairService.getKeyId())
                    .signWith(signingKey, SignatureAlgorithm.HS256)
                    .compact();
            
            DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT
                    .withZone(ZoneId.of("UTC"));
            String issuedAtStr = formatter.format(Instant.ofEpochMilli(issuedAt));
            
            TokenResponse response = new TokenResponse(
                    token,
                    tokenConfiguration.getExpiration(),
                    scope,
                    issuedAtStr
            );
            
            LOG.info("JWT token generated successfully for client: {}, traceId: {}", clientId, traceId);
            MDC.put("traceId", traceId);
            
            return response;
        } catch (Exception e) {
            LOG.error("Error generating JWT token for client: {}, traceId: {}", 
                     request.getClientId(), traceId, e);
            throw new RuntimeException("Failed to generate token", e);
        }
    }
    
    public Claims validateToken(String token) {
        try {
            Key signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            LOG.debug("Token validated successfully for subject: {}", claims.getSubject());
            return claims;
        } catch (Exception e) {
            LOG.error("Token validation failed", e);
            throw new RuntimeException("Invalid token", e);
        }
    }
    
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    public String getSubjectFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.getSubject();
    }
    
    public String getTraceIdFromToken(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.get("trace_id", String.class);
        } catch (Exception e) {
            return null;
        }
    }
}
