package com.survey_engine.user.dto;

/**
 * Response DTO for User entity
 * @param id The user's ID.
 * @param name The user's name.
 * @param email The user's email.
 * @param tenantId The ID of the tenant the user belongs to.
 */
public record UserResponse(
        Long id,
        String name,
        String email,
        String department,
        String region,
        String branch,
        Long tenantId,
        String tenantName
) {}
