package com.survey_engine.user.dto;

/**
 * Response Data Transfer Object (DTO) for authentication.
 *
 * @param token The JWT token issued upon successful authentication.
 */
public record AuthResponse(
        String token
) {}