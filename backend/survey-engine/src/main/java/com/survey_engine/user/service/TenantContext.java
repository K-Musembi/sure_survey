package com.survey_engine.user.service;

import org.springframework.stereotype.Component;

/**
 * A thread-local context for storing and retrieving the current tenant ID.
 * This is crucial for multi-tenancy, ensuring that all operations are scoped
 * to the correct tenant within a single request.
 */
@Component
public class TenantContext {

    private static final ThreadLocal<Long> tenantId = new ThreadLocal<>();

    /**
     * Sets the tenant ID for the current thread.
     *
     * @param id The ID of the tenant.
     */
    public static void setTenantId(Long id) {
        tenantId.set(id);
    }

    /**
     * Retrieves the tenant ID for the current thread.
     *
     * @return The ID of the current tenant.
     */
    public static Long getTenantId() {
        return tenantId.get();
    }

    /**
     * Clears the tenant ID from the current thread's context.
     * This should be called at the end of a request to prevent memory leaks and
     * ensure proper context isolation.
     */
    public static void clear() {
        tenantId.remove();
    }
}
