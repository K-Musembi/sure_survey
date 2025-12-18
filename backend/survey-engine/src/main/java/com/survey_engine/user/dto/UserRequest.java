package com.survey_engine.user.dto;

import com.survey_engine.user.config.security.xss.Sanitize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$",
                message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number.")
        String password,

        @Sanitize
        @Size(min = 3, max = 20, message = "Role must be between 3 and 20 characters")
        String role,

        @Sanitize
        @Size(min = 3, max = 100, message = "Department must be between 3 and 100 characters")
        String department,

        @Sanitize
        @Size(min = 3, max = 100, message = "Region must be between 3 and 100 characters")
        String region,

        @Sanitize
        @Size(min = 3, max = 100, message = "Branch must be between 3 and 100 characters")
        String branch,

        Long tenantId
) {}
