package com.survey_engine.survey.dto.cost;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Request DTO for calculating survey cost.
 * Provide either targetRespondents or budget (not both). smsContactCount is optional.
 */
public record SurveyCostCalculationRequest(
        @Positive(message = "Target respondents must be positive")
        Integer targetRespondents,

        @Positive(message = "Budget must be positive")
        BigDecimal budget,

        // Number of contacts in the selected SMS distribution list (for SMS cost estimation)
        @Positive(message = "SMS contact count must be positive")
        Integer smsContactCount
) {}
