package com.survey_engine.billing.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a new subscription.
 *
 * @param planId The ID of the plan to subscribe to.
 */
public record SubscriptionRequest(
        @NotNull(message = "Plan ID is required")
        Long planId
) {}
