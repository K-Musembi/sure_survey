package com.survey_engine.user.dto;

import com.survey_engine.user.config.security.xss.Sanitize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request Data Transfer Object (DTO) for user login.
 *
 * @param email The user's email address.
 * @param password The user's password.
 */
public record LoginRequest(
        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        @Pattern(regexp = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$", message = "Email should be valid")
        String email,

        @Sanitize
        @NotBlank(message = "Password is required")
        String password
) {}