package com.survey_engine.survey.dto.cost;

import java.math.BigDecimal;

/**
 * Response DTO for survey cost calculation.
 * totalCost = estimatedCost (respondents) + smsCost (distribution).
 */
public record SurveyCostCalculationResponse(
        Integer targetRespondents,
        BigDecimal estimatedCost,       // Respondent activation cost
        BigDecimal costPerRespondent,
        Integer smsContactCount,        // Number of SMS recipients (null if no list selected)
        BigDecimal smsCostPerMessage,   // KES per SMS
        BigDecimal smsCost,             // smsContactCount × smsCostPerMessage
        BigDecimal totalCost,           // estimatedCost + smsCost
        BigDecimal currentWalletBalance,
        boolean isSufficientFunds,
        BigDecimal requiredTopUpAmount
) {}
