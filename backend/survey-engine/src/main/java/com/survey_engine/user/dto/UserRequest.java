package com.survey_engine.user.dto;

import com.survey_engine.user.config.security.xss.Sanitize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for User entity.
 *
 * @param name The user's full name.
 * @param email The user's email address.
 * @param password The user's password.
 * @param role The user's role (e.g., "REGULAR", "ADMIN").
 * @param tenantId The ID of the tenant the user belongs to.
 */
public record UserRequest(
        @Sanitize
        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Sanitize
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        @Sanitize
        @Size(min = 3, max = 20, message = "Role must be between 3 and 20 characters")
        String role,

        @Sanitize
        @Size(min = 3, max = 100, message = "Department must be between 3 and 100 characters")
        String department,

        Long tenantId
) {}
