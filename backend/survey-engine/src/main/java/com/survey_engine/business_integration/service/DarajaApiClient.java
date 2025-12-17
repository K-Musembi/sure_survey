package com.survey_engine.business_integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.Map;

/**
 * Service for interacting with the Safaricom Daraja API.
 * Handles authentication (OAuth) and C2B URL registration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DarajaApiClient {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${daraja.api.base-url}")
    private String darajaBaseUrl;

    /**
     * Authenticates with Daraja and retrieves an OAuth access token.
     *
     * @param consumerKey    The consumer key for the app.
     * @param consumerSecret The consumer secret for the app.
     * @return The access token string.
     */
    public String getAccessToken(String consumerKey, String consumerSecret) {
        String authString = consumerKey + ":" + consumerSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes());

        try {
            String response = webClientBuilder.baseUrl(darajaBaseUrl).build()
                    .get()
                    .uri("/oauth/v1/generate?grant_type=client_credentials")
                    .header("Authorization", "Basic " + encodedAuth)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode rootNode = objectMapper.readTree(response);
            return rootNode.path("access_token").asText();
        } catch (Exception e) {
            log.error("Failed to get Daraja Access Token", e);
            throw new RuntimeException("Failed to authenticate with Daraja: " + e.getMessage());
        }
    }

    /**
     * Registers validation and confirmation URLs for C2B transactions.
     *
     * @param accessToken     The OAuth access token.
     * @param shortCode       The Paybill or Till Number.
     * @param validationUrl   The validation callback URL.
     * @param confirmationUrl The confirmation callback URL.
     * @return The raw response body from Daraja.
     */
    public String registerUrl(String accessToken, String shortCode, String validationUrl, String confirmationUrl) {
        Map<String, String> payload = Map.of(
                "ShortCode", shortCode,
                "ResponseType", "Completed", // We default to Completed if validation endpoint is unreachable
                "ConfirmationURL", confirmationUrl,
                "ValidationURL", validationUrl
        );

        try {
            return webClientBuilder.baseUrl(darajaBaseUrl).build()
                    .post()
                    .uri("/mpesa/c2b/v1/registerurl")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to register C2B URLs", e);
            throw new RuntimeException("Failed to register URLs with Daraja: " + e.getMessage());
        }
    }
}
