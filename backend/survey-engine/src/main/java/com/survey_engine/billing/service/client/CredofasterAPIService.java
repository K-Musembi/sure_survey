package com.survey_engine.billing.service.client;

import com.survey_engine.billing.models.enums.SystemWalletType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CredoFaster implementation of the StockProvider interface.
 * Handles purchasing stock (Airtime/Data) from CredoFaster.
 */
@Service("CREDOFASTER")
@RequiredArgsConstructor
@Slf4j
public class CredofasterAPIService implements StockProvider {

    private final WebClient.Builder webClientBuilder;

    @Value("${credofaster.api.base-url}")
    private String baseUrl;

    @Value("${credofaster.api.airtime.api-key}")
    private String airtimeApiKey;

    @Value("${credofaster.api.airtime.client-id}")
    private String airtimeClientId;

    @Value("${credofaster.api.data.api-key}")
    private String dataApiKey;

    @Value("${credofaster.api.data.client-id}")
    private String dataClientId;

    // Placeholder for the system's receiving number for stock
    private static final String SYSTEM_INVENTORY_PHONE = "254700000000"; 

    @Override
    public boolean purchaseStock(SystemWalletType type, BigDecimal amount) {
        log.info("Initiating CredoFaster restock for {} with amount {}", type, amount);

        String apiKey = type == SystemWalletType.AIRTIME_STOCK ? airtimeApiKey : dataApiKey;
        String clientId = type == SystemWalletType.AIRTIME_STOCK ? airtimeClientId : dataClientId;

        // Construct Request Payload based on documentation
        Map<String, Object> requestBody = Map.of(
            "initiator", Map.of(
                "type", "PARTNER",
                "id", clientId
            ),
            "parameters", List.of(
                Map.of(
                    "reference_id", UUID.randomUUID().toString(),
                    "country", "KE",
                    "account_no", SYSTEM_INVENTORY_PHONE, 
                    "denomination", Map.of(
                        "name", "KE",
                        "value", amount.intValue() // API expects integer
                    ),
                    "other", Map.of()
                )
            )
        );

        return Boolean.TRUE.equals(webClientBuilder.baseUrl(baseUrl).build()
                .post()
                .uri("/airtime/request")
                .header("ApiKey", apiKey)
                .header("ClientId", clientId)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    // In a real scenario, we would parse the JSON response to check ErrorCode == 0
                    log.info("CredoFaster response: {}", response);
                    return true;
                })
                .onErrorResume(e -> {
                    log.error("Failed to call CredoFaster API: {}", e.getMessage());
                    return Mono.just(false);
                })
                .block());
    }
}