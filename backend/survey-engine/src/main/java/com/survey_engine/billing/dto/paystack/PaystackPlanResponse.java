package com.survey_engine.billing.dto.paystack;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for fetching a plan from Paystack.
 *
 * @param status Indicates if the API call was successful.
 * @param message A descriptive message about the API call result.
 * @param name The name of the plan.
 * @param planCode The plan code (e.g., PLN_xxxxxx).
 * @param amount The amount of the plan in the smallest currency unit.
 * @param interval The billing interval (e.g., "monthly", "annually").
 */
public record PaystackPlanResponse(
        boolean status,
        String message,
        String name,
        @JsonProperty("plan_code")
        String planCode,
        Long amount,
        String interval
) {}
