package com.survey_engine.user.dto;

import com.survey_engine.user.config.security.xss.Sanitize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request Data Transfer Object (DTO) for creating a new participant.
 *
 * @param fullName The full name of the participant.
 * @param phoneNumber The phone number of the participant.
 * @param email The email address of the participant.
 */
public record ParticipantRequest(
        @Sanitize
        @NotBlank(message = "Full name is required")
        String fullName,

        @Sanitize
        @NotBlank(message = "Phone number is required")
        String phoneNumber,

        @Sanitize
        @Email(message = "A valid email is required")
        String email
) {}