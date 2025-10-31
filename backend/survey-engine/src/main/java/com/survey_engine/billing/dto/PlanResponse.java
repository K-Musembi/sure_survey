package com.survey_engine.billing.dto;

import com.survey_engine.billing.models.enums.PlanInterval;

import java.math.BigDecimal;

/**
 * Response DTO for a {@link com.survey_engine.billing.models.Plan} entity.
 *
 * @param id The unique identifier of the plan.
 * @param name The name of the plan.
 * @param price The price of the plan.
 * @param billingInterval The billing interval of the plan.
 * @param features A description of the features included in the plan.
 */
public record PlanResponse(
        Long id,
        String name,
        BigDecimal price,
        PlanInterval billingInterval,
        String features
) {}