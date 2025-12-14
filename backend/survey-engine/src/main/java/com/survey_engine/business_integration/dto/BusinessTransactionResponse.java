package com.survey_engine.business_integration.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BusinessTransactionResponse(
        UUID id,
        UUID integrationId,
        String externalTransactionId,
        String msisdn,
        String firstName,
        String lastName,
        BigDecimal amount,
        LocalDateTime transactionTime,
        LocalDateTime createdAt
) {}
