package com.survey_engine.billing.dto;

import com.survey_engine.billing.models.enums.SubscriptionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for a {@link com.survey_engine.billing.models.Subscription} entity.
 *
 * @param id The unique identifier of the subscription.
 * @param plan The details of the subscribed plan.
 * @param status The current status of the subscription.
 * @param currentPeriodStart The start of the current billing period.
 * @param currentPeriodEnd The end of the current billing period.
 * @param trialEndDate The end date of the trial period, if applicable.
 */
public record SubscriptionResponse(
        UUID id,
        PlanResponse plan,
        SubscriptionStatus status,
        LocalDateTime currentPeriodStart,
        LocalDateTime currentPeriodEnd,
        LocalDateTime trialEndDate
) {}