package com.survey_engine.billing.service.client;

import com.survey_engine.billing.models.enums.SystemWalletType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Safaricom implementation of the StockProvider interface.
 * Handles purchasing stock (Airtime/Data) from Safaricom.
 */
@Service("SAFARICOM")
@RequiredArgsConstructor
@Slf4j
public class SafaricomAPIService implements StockProvider {

    private final WebClient.Builder webClientBuilder;

    @Value("${safaricom.api.url}")
    private String safaricomApiUrl;

    @Override
    public boolean purchaseStock(SystemWalletType type, BigDecimal amount) {
        log.info("Initiating Safaricom restock for {} with amount {}", type, amount);

        // Perform External API Call (WebFlux)
        return Boolean.TRUE.equals(webClientBuilder.baseUrl(safaricomApiUrl).build()
                .post()
                .bodyValue(String.format("{\"command\": \"PURCHASE\", \"type\": \"%s\", \"amount\": %s}", type, amount))
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> true)
                .onErrorResume(e -> {
                    log.error("Failed to call Safaricom API: {}", e.getMessage());
                    return Mono.just(false);
                })
                .block());
    }
}