package com.survey_engine.survey.dto.cost;

import java.math.BigDecimal;

/**
 * Response DTO for survey cost calculation.
 */
public record SurveyCostCalculationResponse(
        Integer targetRespondents,
        BigDecimal estimatedCost,
        BigDecimal costPerRespondent,
        BigDecimal currentWalletBalance,
        boolean isSufficientFunds,
        BigDecimal requiredTopUpAmount
) {}
