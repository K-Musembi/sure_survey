package com.survey_engine.user.dto;

/**
 * Response DTO for User entity
 * @param id
 * @param name
 * @param email
 * @param companyId
 */
public record UserResponse(
        Long id,
        String name,
        String email,
        Long companyId
) {}
