package com.survey_engine.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request Data Transfer Object (DTO) for user login.
 *
 * @param email The user's email address.
 * @param password The user's password.
 */
public record LoginRequest(
        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {}