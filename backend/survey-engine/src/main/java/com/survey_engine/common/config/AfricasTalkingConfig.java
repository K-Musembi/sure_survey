package com.survey_engine.common.config;

import com.africastalking.AfricasTalking;
import com.africastalking.AirtimeService;
import com.africastalking.SmsService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Centralized configuration for the Africa's Talking SDK.
 * Provides beans for various services like SMS and Airtime.
 */
@Configuration
public class AfricasTalkingConfig {

    /**
     * Loads Africa's Talking credentials from application properties.
     */
    @Configuration
    @ConfigurationProperties(prefix = "africastalking.api")
    @Data
    @Validated
    public static class AfricasTalkingProperties {

        @NotBlank(message = "Africa's Talking username must be configured")
        private String username;

        @NotBlank(message = "Africa's Talking API key must be configured")
        private String apiKey;
    }

    /**
     * Creates the SmsService bean.
     * @param properties The configured API credentials.
     * @return An instance of SmsService.
     */
    @Bean
    public SmsService africasTalkingSmsService(AfricasTalkingProperties properties) {
        // Initialization is idempotent and safe to call multiple times.
        AfricasTalking.initialize(properties.getUsername(), properties.getApiKey());
        return AfricasTalking.getService(AfricasTalking.SERVICE_SMS);
    }

    /**
     * Creates the AirtimeService bean.
     * @param properties The configured API credentials.
     * @return An instance of AirtimeService.
     */
    @Bean
    public AirtimeService africasTalkingAirtimeService(AfricasTalkingProperties properties) {
        AfricasTalking.initialize(properties.getUsername(), properties.getApiKey());
        return AfricasTalking.getService(AfricasTalking.SERVICE_AIRTIME);
    }
}

