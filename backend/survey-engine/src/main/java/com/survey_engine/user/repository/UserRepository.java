package com.survey_engine.user.repository;

import com.survey_engine.user.dto.TenantUserCount;
import com.survey_engine.user.models.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides CRUD operations and custom queries for users.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a User entity by its email address.
     *
     * @param email The email address of the user.
     * @return An {@link Optional} containing the found User or empty if not found.
     */
    Optional<User> findByEmail(String email);

    @EntityGraph(value = "User.tenant")
    Optional<User> findByEmailWithTenant(String email);

    /**
     * Finds a User entity by its email address and tenant ID.
     *
     * @param email The email address of the user.
     * @param tenantId The ID of the tenant.
     * @return An {@link Optional} containing the found User or empty if not found.
     */
    Optional<User> findByEmailAndTenantId(String email, Long tenantId);

    /**
     * Finds a User entity by its ID and tenant ID.
     *
     * @param id The ID of the user.
     * @param tenantId The ID of the tenant.
     * @return An {@link Optional} containing the found User or empty if not found.
     */
    Optional<User> findByIdAndTenantId(Long id, Long tenantId);

    /**
     * Finds all User entities belonging to a specific tenant.
     *
     * @param tenantId The ID of the tenant.
     * @return A list of User entities for the given tenant.
     */
    List<User> findByTenantId(Long tenantId);

    /**
     * Finds all User entities belonging to a specific tenant and department.
     *
     * @param tenantId The ID of the tenant.
     * @param department The department of the user.
     * @return A list of User entities for the given tenant and department.
     */
    List<User> findByTenantIdAndDepartment(Long tenantId, String department);

    /**
     * Counts users and groups them by tenant ID.
     *
     * @return A list of {@link TenantUserCount} projections.
     */
    @Query("SELECT new com.survey_engine.user.dto.TenantUserCount(u.tenantId, COUNT(u.id)) FROM User u WHERE u.tenantId IS NOT NULL GROUP BY u.tenantId")
    List<TenantUserCount> countUsersByTenant();
}