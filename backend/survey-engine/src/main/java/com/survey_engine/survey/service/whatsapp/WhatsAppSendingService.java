package com.survey_engine.survey.service.whatsapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Sends WhatsApp messages via the WhatsApp Business API (Cloud API).
 * Uses the Meta Graph API to send template and text messages.
 */
@Service
@Slf4j
public class WhatsAppSendingService {

    private final WebClient webClient;
    private final String phoneNumberId;

    public WhatsAppSendingService(
            @Value("${whatsapp.api.base-url:https://graph.facebook.com/v18.0}") String baseUrl,
            @Value("${whatsapp.api.access-token:}") String accessToken,
            @Value("${whatsapp.api.phone-number-id:}") String phoneNumberId) {
        this.phoneNumberId = phoneNumberId;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Sends a text message to a WhatsApp recipient.
     *
     * @param to      Recipient phone number in international format (e.g., 254712345678).
     * @param message The text message content.
     */
    public void sendTextMessage(String to, String message) {
        log.info("Sending WhatsApp text message to: {}", to);
        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", to,
                "type", "text",
                "text", Map.of("body", message)
        );
        sendMessage(payload);
    }

    /**
     * Sends a template message (required for initiating conversations).
     *
     * @param to           Recipient phone number.
     * @param templateName The approved template name.
     * @param languageCode Language code (e.g., "en").
     */
    public void sendTemplateMessage(String to, String templateName, String languageCode) {
        log.info("Sending WhatsApp template '{}' to: {}", templateName, to);
        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", to,
                "type", "template",
                "template", Map.of(
                        "name", templateName,
                        "language", Map.of("code", languageCode)
                )
        );
        sendMessage(payload);
    }

    private void sendMessage(Map<String, Object> payload) {
        try {
            String response = webClient.post()
                    .uri("/{phoneNumberId}/messages", phoneNumberId)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("WhatsApp API response: {}", response);
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message: {}", e.getMessage());
        }
    }
}
