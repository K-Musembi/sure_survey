package com.survey_engine.rewards.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for requests related to a user's loyalty account.
 *
 * @param userId The unique identifier for the user.
 */
public record LoyaltyAccountRequest(
        @NotBlank(message = "User ID cannot be blank")
        String userId
) {
}