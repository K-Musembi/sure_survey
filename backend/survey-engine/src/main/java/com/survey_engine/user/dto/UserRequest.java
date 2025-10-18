package com.survey_engine.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for User entity
 * @param name
 * @param email
 * @param password
 * @param role
 * @param tenantId
 */
public record UserRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        @Size(min = 3, max = 20, message = "Role must be between 3 and 20 characters")
        String role,

        Long tenantId
) {}
