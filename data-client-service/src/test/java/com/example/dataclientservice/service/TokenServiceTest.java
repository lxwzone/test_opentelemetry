package com.example.dataclientservice.service;

import com.example.dataclientservice.dto.TokenRequest;
import com.example.dataclientservice.dto.TokenResponse;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MicronautTest
class TokenServiceTest {

    @Inject
    @Client("${client.token.service.url}")
    private HttpClient tokenServiceClient;

    @Inject
    private TokenService tokenService;

    @MockBean(HttpClient.class)
    HttpClient httpClient() {
        return Mockito.mock(HttpClient.class);
    }

    @BeforeEach
    void setUp() {
        tokenService.invalidateToken();
    }

    @Test
    @DisplayName("Should get access token successfully")
    void testGetAccessToken() {
        TokenResponse mockResponse = new TokenResponse();
        mockResponse.setAccessToken("test-access-token");
        mockResponse.setTokenType("Bearer");
        mockResponse.setExpiresIn(3600L);

        HttpResponse<TokenResponse> httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus().getCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(mockResponse);

        when(tokenServiceClient.toBlocking()).thenReturn(mock(io.micronaut.http.client.BlockingHttpClient.class));
        when(tokenServiceClient.toBlocking().exchange(any(HttpRequest.class), any(Class.class)))
                .thenReturn(httpResponse);

        String token = tokenService.getAccessToken();

        assertNotNull(token);
        assertEquals("test-access-token", token);
    }

    @Test
    @DisplayName("Should throw exception when token service fails")
    void testGetAccessTokenFailure() {
        when(tokenServiceClient.toBlocking()).thenReturn(mock(io.micronaut.http.client.BlockingHttpClient.class));
        when(tokenServiceClient.toBlocking().exchange(any(HttpRequest.class), any(Class.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        assertThrows(RuntimeException.class, () -> {
            tokenService.getAccessToken();
        });
    }

    @Test
    @DisplayName("Should invalidate token successfully")
    void testInvalidateToken() {
        assertDoesNotThrow(() -> {
            tokenService.invalidateToken();
        });
    }

    @Test
    @DisplayName("Should cache token and reuse it")
    void testTokenCaching() {
        TokenResponse mockResponse = new TokenResponse();
        mockResponse.setAccessToken("cached-token");
        mockResponse.setTokenType("Bearer");
        mockResponse.setExpiresIn(3600L);

        HttpResponse<TokenResponse> httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus().getCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(mockResponse);

        when(tokenServiceClient.toBlocking()).thenReturn(mock(io.micronaut.http.client.BlockingHttpClient.class));
        when(tokenServiceClient.toBlocking().exchange(any(HttpRequest.class), any(Class.class)))
                .thenReturn(httpResponse);

        String token1 = tokenService.getAccessToken();
        String token2 = tokenService.getAccessToken();

        assertEquals(token1, token2);
        verify(tokenServiceClient.toBlocking(), times(1)).exchange(any(HttpRequest.class), any(Class.class));
    }
}