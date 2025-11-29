package com.survey_engine.billing.dto;

import com.survey_engine.billing.models.enums.WalletTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for a wallet transaction.
 *
 * @param id The unique ID of the transaction.
 * @param amount The amount transacted.
 * @param type The type of transaction (CREDIT, DEBIT).
 * @param referenceId The external reference (e.g. Payment ID or Survey ID).
 * @param description A human-readable description.
 * @param createdAt When the transaction occurred.
 */
public record WalletTransactionResponse(
        UUID id,
        BigDecimal amount,
        WalletTransactionType type,
        String referenceId,
        String description,
        LocalDateTime createdAt
) {}
