package com.survey_engine.user.dto;

import com.survey_engine.user.config.security.xss.Sanitize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request Data Transfer Object (DTO) for user sign-up.
 *
 * @param name The user's full name.
 * @param email The user's email address.
 * @param password The user's chosen password.
 * @param role The user's role (e.g., "REGULAR", "ADMIN").
 * @param tenantId The ID of the tenant the user belongs to (optional for individual sign-up).
 */
public record SignUpRequest(
        @Sanitize
        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        String name,

        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        String email,

        @Sanitize
        @NotBlank(message = "Password is required")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number.")
        String password,

        @Sanitize
        @Size(min = 3, max = 20, message = "Role must be between 3 and 20 characters")
        String role,

        Long tenantId
) {}