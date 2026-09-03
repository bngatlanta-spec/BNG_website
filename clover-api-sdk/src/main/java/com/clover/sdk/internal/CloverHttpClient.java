package com.clover.sdk.internal;

import com.clover.sdk.CloverConfig;
import com.clover.sdk.exception.CloverException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Low-level HTTP client for the Clover REST API.
 * Handles auth headers, JSON serialization, and 429 retry logic.
 */
public class CloverHttpClient {

    private static final int MAX_RETRIES = 2;
    private static final long RETRY_DELAY_MS = 1500;

    private final CloverConfig config;
    private final HttpClient httpClient;
    public final ObjectMapper mapper;

    public CloverHttpClient(CloverConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();
    }

    public JsonNode get(String path) throws CloverException {
        String url = config.getBaseUrl() + path;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + config.getToken())
                .header("Content-Type", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(30))
                .build();
        return execute(request, MAX_RETRIES);
    }

    public JsonNode post(String path, Object payload) throws CloverException {
        String url = config.getBaseUrl() + path;
        String body;
        try {
            body = mapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new CloverException("Failed to serialize request payload", e);
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + config.getToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(30))
                .build();
        return execute(request, MAX_RETRIES);
    }

    /**
     * Bare POST with no body — used for Clover relationship endpoints
     * (e.g., linking a customer to an order).
     */
    public JsonNode postEmpty(String path) throws CloverException {
        String url = config.getBaseUrl() + path;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + config.getToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofSeconds(30))
                .build();
        return execute(request, MAX_RETRIES);
    }

    private JsonNode execute(HttpRequest request, int retriesLeft) throws CloverException {
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new CloverException("Network error communicating with Clover API", e);
        }

        if (response.statusCode() == 429 && retriesLeft > 0) {
            try {
                Thread.sleep(RETRY_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return execute(request, retriesLeft - 1);
        }

        String responseBody = response.body();
        JsonNode json;
        try {
            json = mapper.readTree(responseBody.isBlank() ? "{}" : responseBody);
        } catch (JsonProcessingException e) {
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return mapper.createObjectNode();
            }
            throw new CloverException("Non-JSON error response from Clover [HTTP " + response.statusCode() + "]: " + responseBody,
                    response.statusCode(), responseBody);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String message = json.path("message").asText(null);
            if (message == null || message.isBlank()) {
                message = "HTTP " + response.statusCode();
            }
            throw new CloverException(message, response.statusCode(), responseBody);
        }

        return json;
    }

    /**
     * Charges a card via Clover Ecommerce API.
     * @param amountCents amount in cents (e.g. 1899 for $18.99)
     * @param cardToken   token produced by Clover.js on the frontend
     */
    public JsonNode chargeCard(long amountCents, String cardToken) throws CloverException {
        String url = config.getEcommerceBaseUrl() + "/v1/charges";
        Map<String, Object> payload = Map.of(
            "amount",   amountCents,
            "currency", "usd",
            "source",   cardToken
        );
        String body;
        try {
            body = mapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new CloverException("Failed to serialize charge request", e);
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + config.getToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(30))
                .build();
        return execute(request, MAX_RETRIES);
    }

    public String getMerchantId() {
        return config.getMerchantId();
    }

    public String getAppId() {
        return config.getAppId();
    }

    public String getPakmsKey() {
        return config.getPakmsKey();
    }

    public String getJsSdkUrl() {
        return config.getJsSdkUrl();
    }
}
