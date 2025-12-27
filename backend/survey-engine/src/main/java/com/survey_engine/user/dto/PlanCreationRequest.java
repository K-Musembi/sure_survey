package com.survey_engine.user.dto;

import com.survey_engine.user.config.security.xss.Sanitize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Request DTO for creating a new subscription plan (User module).
 */
public record PlanCreationRequest(
        @Sanitize
        @NotBlank(message = "Plan name is required")
        String name,

        @NotNull(message = "Price is required")
        @PositiveOrZero(message = "Price must be zero or positive")
        BigDecimal price,

        @Sanitize
        @NotBlank(message = "Billing interval is required (e.g., MONTHLY, YEARLY)")
        String interval,

        Map<String, Object> features
) {}
