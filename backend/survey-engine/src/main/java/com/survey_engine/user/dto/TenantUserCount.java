package com.survey_engine.user.dto;

/**
 * A projection DTO for carrying the result of a user count query grouped by tenant.
 *
 * @param tenantId The ID of the tenant.
 * @param userCount The number of users for the tenant.
 */
public record TenantUserCount(
        Long tenantId,
        Long userCount) {
}
