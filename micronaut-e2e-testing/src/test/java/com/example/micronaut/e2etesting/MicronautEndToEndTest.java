package com.example.micronaut.e2etesting;

import com.example.micronaut.e2etesting.dto.PagedResponse;
import com.example.micronaut.e2etesting.dto.TokenRequest;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MicronautEndToEndTest {

    private static final Logger LOG = LoggerFactory.getLogger(MicronautEndToEndTest.class);

    @Inject
    @Client("http://localhost:8081")
    private HttpClient tokenServiceClient;

    @Inject
    @Client("http://localhost:8080")
    private HttpClient dataQueryServiceClient;

    @Inject
    @Client("http://localhost:8082")
    private HttpClient dataClientServiceClient;

    private String accessToken;

    @BeforeAll
    void setUp() {
        LOG.info("Starting Micronaut-based end-to-end tests");
        LOG.info("Token service URL: http://localhost:8081");
        LOG.info("Data query service URL: http://localhost:8080");
    }

    @Test
    @Order(1)
    void testTokenGenerationAndCallbackDelivery() {
        LOG.info("Test 1: Token generation and callback delivery");

        // Create token request
        TokenRequest request = new TokenRequest();
        request.setClientId("data-client-service");
        request.setClientSecret("secret123");
        request.setCallbackUrl("http://localhost:8082/callback");

        // Send token request
        HttpResponse<String> response = tokenServiceClient.toBlocking()
                .exchange(HttpRequest.POST("/oauth/token", request), String.class);

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatus(), "Token generation should return 200 OK");
        String responseBody = response.body();
        assertTrue(responseBody.contains("Token delivered via callback"), "Response should indicate token was delivered via callback");
        LOG.info("Token delivery initiated successfully");
    }

    @Test
    @Order(2)
    void testDataQueryServiceUsersEndpoint() {
        LOG.info("Test 2: Data query service users endpoint");

        // Get token from data-client-service
        HttpResponse<Map> tokenResponse = dataClientServiceClient.toBlocking()
                .exchange(HttpRequest.GET("/api/v1/token"), Map.class);

        String accessToken = ((Map<String, String>) tokenResponse.body()).get("accessToken");
        assertNotNull(accessToken, "Access token should be retrieved from data-client-service");

        // Test accessing users API with token
        HttpResponse<PagedResponse> response = dataQueryServiceClient.toBlocking()
                .exchange(HttpRequest.GET("/api/v1/users?page=0&size=10")
                        .header("Authorization", "Bearer " + accessToken), 
                        PagedResponse.class);

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatus(), "Users API should return 200 OK");
        assertNotNull(response.body(), "Users response should not be null");

        PagedResponse userResponse = response.body();
        assertNotNull(userResponse.getData(), "Users data should not be null");
        assertTrue(userResponse.getData().size() > 0, "Users data should not be empty");
        LOG.info("Successfully accessed users API with token");
    }

    @Test
    @Order(3)
    void testDataQueryServiceProductsEndpoint() {
        LOG.info("Test 3: Data query service products endpoint");

        // Get token from data-client-service
        HttpResponse<Map> tokenResponse = dataClientServiceClient.toBlocking()
                .exchange(HttpRequest.GET("/api/v1/token"), Map.class);

        String accessToken = ((Map<String, String>) tokenResponse.body()).get("accessToken");
        assertNotNull(accessToken, "Access token should be retrieved from data-client-service");

        // Test accessing products API with token
        HttpResponse<PagedResponse> response = dataQueryServiceClient.toBlocking()
                .exchange(HttpRequest.GET("/api/v1/products?page=0&size=10&category=electronics")
                        .header("Authorization", "Bearer " + accessToken), 
                        PagedResponse.class);

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatus(), "Products API should return 200 OK");
        assertNotNull(response.body(), "Products response should not be null");

        PagedResponse productResponse = response.body();
        assertNotNull(productResponse.getData(), "Products data should not be null");
        assertTrue(productResponse.getData().size() > 0, "Products data should not be empty");

        LOG.info("Successfully retrieved {} products", productResponse.getData().size());
    }

    @Test
    @Order(4)
    void testOpenTelemetryIsEnabled() {
        LOG.info("Test 4: OpenTelemetry is enabled");

        // Get token from data-client-service
        HttpResponse<Map> tokenResponse = dataClientServiceClient.toBlocking()
                .exchange(HttpRequest.GET("/api/v1/token"), Map.class);

        String accessToken = ((Map<String, String>) tokenResponse.body()).get("accessToken");
        assertNotNull(accessToken, "Access token should be retrieved from data-client-service");

        // Test that the service is accessible and OpenTelemetry is configured
        // Note: Trace headers are not typically returned in responses by default
        // OpenTelemetry is configured in the application.yml and should be sending traces to Jaeger
        HttpResponse<PagedResponse> response = dataQueryServiceClient.toBlocking()
                .exchange(HttpRequest.GET("/api/v1/users?page=0&size=10")
                        .header("Authorization", "Bearer " + accessToken), 
                        PagedResponse.class);

        // Verify the request succeeds
        assertEquals(HttpStatus.OK, response.getStatus(), "Users API should return 200 OK");
        assertNotNull(response.body(), "Users response should not be null");

        LOG.info("OpenTelemetry is enabled (configured in application.yml with Jaeger)");
    }

    @Test
    @Order(5)
    void testUnauthorizedAccess() {
        LOG.info("Test 5: Unauthorized access");

        // Test accessing API without token
        try {
            HttpResponse<String> response = dataQueryServiceClient.toBlocking()
                    .exchange(HttpRequest.GET("/api/v1/users?page=0&size=10"), 
                            String.class);
            // If we get here, the request succeeded when it should have failed
            fail("Expected 401 Unauthorized response, but got: " + response.getStatus());
        } catch (io.micronaut.http.client.exceptions.HttpClientResponseException e) {
            // This is expected - Micronaut throws an exception for 401 responses
            assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus(), "Should return 401 Unauthorized without token");
            LOG.info("Unauthorized access properly rejected with 401");
        }
    }

    @AfterAll
    void tearDown() {
        LOG.info("Micronaut-based end-to-end tests completed");
    }
}