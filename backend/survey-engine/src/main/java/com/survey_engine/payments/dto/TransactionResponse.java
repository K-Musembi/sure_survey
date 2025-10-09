package com.payments.payments.dto;

import com.payments.payments.models.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response Data Transfer Object (DTO) for a completed Transaction.
 *
 * @param id                   The unique ID of the transaction.
 * @param paymentId            The ID of the parent payment attempt.
 * @param type                 The type of transaction (e.g., CHARGE, REFUND).
 * @param amount               The amount of the transaction.
 * @param currency             The currency of the transaction.
 * @param gatewayTransactionId The unique ID from the payment gateway.
 * @param processedAt          The timestamp when the transaction was processed.
 */
public record TransactionResponse(
        UUID id,
        UUID paymentId,
        TransactionType type,
        BigDecimal amount,
        String currency,
        String gatewayTransactionId,
        LocalDateTime processedAt
) {}
