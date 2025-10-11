package com.survey_engine.survey.config.sms;

import com.africastalking.AfricasTalking;
import com.africastalking.SmsService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
public class SmsProviderConfig {

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

    @Bean
    public SmsService africasTalkingSmsService(AfricasTalkingProperties properties) {
        AfricasTalking.initialize(properties.getUsername(), properties.getApiKey());
        return AfricasTalking.getService(AfricasTalking.SERVICE_SMS);
    }
}

