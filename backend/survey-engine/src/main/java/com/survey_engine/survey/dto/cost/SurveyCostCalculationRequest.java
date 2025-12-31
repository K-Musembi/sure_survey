package com.survey_engine.survey.dto.cost;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Request DTO for calculating survey cost.
 */
public record SurveyCostCalculationRequest(
        @NotNull(message = "Either targetRespondents or budget must be provided")
        Integer targetRespondents,

        @Positive(message = "Budget must be positive")
        BigDecimal budget
) {}
