package com.example.dataclientservice.controller;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class ClientUserControllerTest {

    @Inject
    @Client("/")
    private HttpClient httpClient;

    @Test
    @DisplayName("Should get all users")
    void testGetAllUsers() {
        HttpResponse<?> response = httpClient.toBlocking().exchange(HttpRequest.GET("/api/v1/users"));

        assertNotNull(response);
    }

    @Test
    @DisplayName("Should get user by ID")
    void testGetUserById() {
        HttpResponse<?> response = httpClient.toBlocking().exchange(HttpRequest.GET("/api/v1/users/1"));

        assertNotNull(response);
    }

    @Test
    @DisplayName("Should get paginated users")
    void testGetPaginatedUsers() {
        HttpResponse<?> response = httpClient.toBlocking().exchange(
                HttpRequest.GET("/api/v1/users?page=0&size=10")
        );

        assertNotNull(response);
    }
}