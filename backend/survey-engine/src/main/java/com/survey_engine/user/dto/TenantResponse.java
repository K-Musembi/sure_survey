package com.survey_engine.user.dto;

/**
 * Response Data Transfer Object (DTO) for Tenant entity.
 *
 * @param id The unique identifier of the tenant.
 * @param name The human-readable name of the tenant.
 * @param slug A unique, URL-friendly identifier for the tenant.
 */
public record TenantResponse(

        Long id,
        String name,
        String slug
) {}
