package com.survey_engine.billing.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PlanUpdateRequest(
        @NotNull Long planId,
        BigDecimal price,
        PlanFeatures features
) {}