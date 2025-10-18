package com.survey_engine.user.dto;

/**
 * Response Data Transfer Object (DTO) for Tenant entity
 * @param id
 * @param name
 * @param slug
 */
public record TenantResponse(

        Long id,
        String name,
        String slug
) {}
