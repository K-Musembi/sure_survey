package com.payments.payments.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.payments.config.PaystackWebclientConfig;
import com.payments.payments.dto.paystack.PaystackWebhookData;
import com.payments.payments.dto.paystack.PaystackWebhookEvent;
import com.payments.payments.service.WebhookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 * Controller to handle incoming webhooks from PayStack.
 * This endpoint is public but secured by signature verification.
 */
@RestController
@RequestMapping("/api/v1/webhooks")
@Slf4j
public class WebhookController {

    private final WebhookService webhookService;
    private final String paystackSecretKey;
    private final ObjectMapper objectMapper;

    /**
     * Constructor for WebhookController.
     *
     * @param webhookService The service for processing webhook business logic.
     * @param paystackConfig The configuration containing the PayStack secret key.
     * @param objectMapper The Jackson ObjectMapper for manual JSON parsing.
     */
    public WebhookController(WebhookService webhookService, PaystackWebclientConfig paystackConfig, ObjectMapper objectMapper) {
        this.webhookService = webhookService;
        this.paystackSecretKey = paystackConfig.getSecretKey();
        this.objectMapper = objectMapper;
    }

    /**
     * Handles incoming webhook events from PayStack.
     *
     * @param payloadBody The raw JSON payload from the request body, used for signature verification.
     * @param signature   The value of the 'x-paystack-signature' header.
     * @return A 200 OK response to acknowledge receipt of the event.
     */
    @PostMapping("/paystack")
    public ResponseEntity<Void> handlePaystackWebhook(
            @RequestBody String payloadBody,
            @RequestHeader("x-paystack-signature") String signature) {

        log.info("Received PayStack webhook.");

        if (!isValidSignature(signature, payloadBody)) {
            log.warn("Invalid webhook signature received. Access denied.");
            // Return 200 OK to prevent PayStack from retrying a malicious request.
            return ResponseEntity.ok().build();
        }

        try {
            PaystackWebhookEvent<PaystackWebhookData> event = objectMapper.readValue(payloadBody, new TypeReference<>() {});
            webhookService.processWebhookEvent(event);
        } catch (Exception e) {
            // Catching potential exceptions from service layer (e.g., DB errors, message queue failures)
            log.error("Error processing webhook event: {}", payloadBody, e);
            // Return a server error to signal to PayStack that processing failed and should be retried.
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Verifies the webhook signature to ensure it came from PayStack.
     *
     * @param signature   The signature from the 'x-paystack-signature' header.
     * @param payloadBody The raw request body.
     * @return True if the signature is valid, false otherwise.
     */
    private boolean isValidSignature(String signature, String payloadBody) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(paystackSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payloadBody.getBytes(StandardCharsets.UTF_8));

            // Convert byte array to hex string
            Formatter formatter = new Formatter();
            for (byte b : hash) {
                formatter.format("%02x", b);
            }
            String computedSignature = formatter.toString();

            return computedSignature.equals(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error while verifying webhook signature.", e);
            return false;
        }
    }
}