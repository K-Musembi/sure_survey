package com.survey_engine.user.repository;

import com.survey_engine.user.models.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Tenant entity.
 * Provides CRUD operations and custom queries for tenants.
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    /**
     * Finds a {@link Tenant} entity by its name.
     *
     * @param name The name of the tenant.
     * @return An {@link Optional} containing the found Tenant or empty if not found.
     */
    Optional<Tenant> findByName(String name);

    /**
     * Finds a {@link Tenant} entity by its unique slug.
     *
     * @param slug The unique slug of the tenant.
     * @return An {@link Optional} containing the found Tenant or empty if not found.
     */
    Optional<Tenant> findBySlug(String slug);
}
