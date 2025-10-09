package com.payments.payments.dto;

import com.payments.payments.models.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A DTO for returning detailed information about a payment.
 */
public record PaymentEventDetails(
        UUID id,
        String surveyId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String gatewayTransactionId,
        LocalDateTime createdAt
) {}