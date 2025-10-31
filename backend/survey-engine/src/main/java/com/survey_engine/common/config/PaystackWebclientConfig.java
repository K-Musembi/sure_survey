package com.survey_engine.common.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration properties for PayStack API using WebClient for asynchronous calls
 * Binds values from application.properties to a type-safe object.
 */
@Configuration
@ConfigurationProperties(prefix = "paystack.api")
@Data
@Validated
public class PaystackWebclientConfig {

    @NotBlank(message = "PayStack secret key must be configured")
    private String secretKey;

    @NotBlank(message = "PayStack base URL must be configured")
    private String baseUrl;

    @Bean
    public WebClient paystackClient(WebClient.Builder builder) {
        return builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
