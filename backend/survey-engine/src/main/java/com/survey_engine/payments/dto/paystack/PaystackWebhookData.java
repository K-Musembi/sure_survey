package com.survey_engine.payments.dto.paystack;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.survey_engine.payments.config.security.xss.Sanitize;

/**
 * Represents the 'data' object within a PayStack webhook event, specifically for 'charge.success'.
 * This record models the expected fields from the PayStack API for a successful charge.
 */
public record PaystackWebhookData(
    @Sanitize
    String reference,
    long amount, // Amount in smallest currency unit (e.g., kobo, cents)
    @Sanitize
    String currency,
    @JsonProperty("id")
    long transactionId, // The unique ID for this specific transaction from PayStack
    @Sanitize
    String status,
    @Sanitize
    String channel
    // 'paid_at', 'customer', 'authorization', etc.
) {}