package com.survey_engine.user.dto;

import java.util.List;

/**
 * Response Data Transfer Object (DTO) for checking tenant name similarity.
 *
 * @param similarTenantNames A list of names of tenants that are similar to the queried tenant name.
 */
public record CheckTenantResponse(
        List<String> similarTenantNames
) {}