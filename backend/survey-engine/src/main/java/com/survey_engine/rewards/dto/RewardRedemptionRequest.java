package com.survey_engine.rewards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for the request to claim a reward after completing a survey, 
 * typically used to provide a phone number for web-based completions.
 *
 * @param surveyId The ID of the survey that was completed.
 * @param phoneNumber The phone number to which the reward should be sent.
 */
public record RewardRedemptionRequest(
        @NotNull(message = "Survey ID is required")
        Long surveyId,

        @NotBlank(message = "Phone number is required")
        String phoneNumber
) {
}