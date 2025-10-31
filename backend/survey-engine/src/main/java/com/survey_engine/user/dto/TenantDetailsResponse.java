package com.survey_engine.user.dto;

import com.survey_engine.user.models.Tenant;

/**
 * A DTO representing detailed information about a tenant, including the number of associated users.
 *
 * @param id The unique identifier of the tenant.
 * @param name The human-readable name of the tenant.
 * @param slug A unique, URL-friendly identifier for the tenant.
 * @param status The current status of the tenant (e.g., ACTIVE, INACTIVE).
 * @param plan The subscription plan of the tenant.
 * @param userCount The total number of users associated with this tenant.
 */
public record TenantDetailsResponse(
        Long id,
        String name,
        String slug,
        String status,
        String plan,
        long userCount
) {
    /**
     * Factory method to create a TenantDetailsResponse from a Tenant entity and a user count.
     *
     * @param tenant The Tenant entity.
     * @param userCount The number of users in the tenant.
     * @return A new TenantDetailsResponse instance.
     */
    public static TenantDetailsResponse from(Tenant tenant, long userCount) {
        return new TenantDetailsResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                tenant.getStatus(),
                tenant.getPlan(),
                userCount
        );
    }
}