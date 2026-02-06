package com.example.tokenservice.service;

import com.example.tokenservice.config.TokenConfiguration;
import com.example.tokenservice.dto.TokenRequest;
import com.example.tokenservice.dto.TokenResponse;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class JwtTokenServiceTest {

    @Inject
    private JwtTokenService jwtTokenService;

    @Inject
    private KeyPairService keyPairService;

    private TokenRequest validTokenRequest;

    @BeforeEach
    void setUp() {
        validTokenRequest = new TokenRequest();
        validTokenRequest.setClientId("test-client");
        validTokenRequest.setClientSecret("test-secret");
        validTokenRequest.setGrantType("client_credentials");
        validTokenRequest.setScope("read write");
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void testGenerateToken() {
        TokenResponse response = jwtTokenService.generateToken(validTokenRequest, "test-trace-id");

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getTokenType());
        assertNotNull(response.getExpiresIn());
        assertNotNull(response.getIssuedAt());
        assertEquals("Bearer", response.getTokenType());
        assertTrue(response.getExpiresIn() > 0);
    }

    @Test
    @DisplayName("Should generate tokens with different values")
    void testGenerateUniqueTokens() {
        TokenResponse response1 = jwtTokenService.generateToken(validTokenRequest, "trace-1");
        TokenResponse response2 = jwtTokenService.generateToken(validTokenRequest, "trace-2");

        assertNotEquals(response1.getAccessToken(), response2.getAccessToken());
    }

    @Test
    @DisplayName("Should validate valid token")
    void testValidateToken() {
        TokenResponse response = jwtTokenService.generateToken(validTokenRequest, "test-trace-id");
        var claims = jwtTokenService.validateToken(response.getAccessToken());

        assertNotNull(claims);
        assertEquals("test-client", claims.getSubject());
    }

    @Test
    @DisplayName("Should throw exception when validating invalid token")
    void testValidateInvalidToken() {
        assertThrows(RuntimeException.class, () -> {
            jwtTokenService.validateToken("invalid.token.here");
        });
    }

    @Test
    @DisplayName("Should check if token is expired")
    void testIsTokenExpired() {
        TokenResponse response = jwtTokenService.generateToken(validTokenRequest, "test-trace-id");
        boolean isExpired = jwtTokenService.isTokenExpired(response.getAccessToken());

        assertFalse(isExpired);
    }

    @Test
    @DisplayName("Should return true for expired token check on invalid token")
    void testIsTokenExpiredForInvalidToken() {
        boolean isExpired = jwtTokenService.isTokenExpired("invalid.token");

        assertTrue(isExpired);
    }

    @Test
    @DisplayName("Should get subject from token")
    void testGetSubjectFromToken() {
        TokenResponse response = jwtTokenService.generateToken(validTokenRequest, "test-trace-id");
        String subject = jwtTokenService.getSubjectFromToken(response.getAccessToken());

        assertNotNull(subject);
        assertEquals("test-client", subject);
    }

    @Test
    @DisplayName("Should get trace ID from token")
    void testGetTraceIdFromToken() {
        String traceId = "test-trace-id-123";
        TokenResponse response = jwtTokenService.generateToken(validTokenRequest, traceId);
        String extractedTraceId = jwtTokenService.getTraceIdFromToken(response.getAccessToken());

        assertNotNull(extractedTraceId);
        assertEquals(traceId, extractedTraceId);
    }

    @Test
    @DisplayName("Should return null for trace ID from invalid token")
    void testGetTraceIdFromInvalidToken() {
        String traceId = jwtTokenService.getTraceIdFromToken("invalid.token");

        assertNull(traceId);
    }

    @Test
    @DisplayName("Should generate token with correct expiration time")
    void testTokenExpiration() {
        TokenResponse response = jwtTokenService.generateToken(validTokenRequest, "test-trace-id");

        assertNotNull(response);
        assertTrue(response.getExpiresIn() > 0);
    }
}