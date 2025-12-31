package com.survey_engine.user;

import com.survey_engine.user.models.Tenant;
import com.survey_engine.user.models.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.modulith.NamedInterface;

import java.util.*;

/**
 * Public API for the User module, exposing tenant-aware user and participant information.
 * This interface allows other modules to interact with user-related data without direct
 * dependencies on the internal implementation details of the 'user' module.
 */
@NamedInterface
public interface UserApi {
    /**
     * Finds the participant ID associated with a given phone number.
     * @param phoneNumber The phone number to search for.
     * @return An {@link Optional} containing the participant ID as a String, or empty if not found.
     */
    Optional<String> findParticipantIdByPhoneNumber(String phoneNumber);

    /**
     * Finds the phone number associated with a given participant ID.
     * @param participantId The participant ID to search for.
     * @return An {@link Optional} containing the phone number as a String, or empty if not found or invalid ID.
     */
    Optional<String> findPhoneNumberByParticipantId(String participantId);

    /**
     * Retrieves the tenant ID from the current context.
     * @return The ID of the current tenant.
     */
    Long getTenantId();

    /**
     * Finds a {@link Tenant} by its ID.
     * @param tenantId The ID of the tenant to find.
     * @return An {@link Optional} containing the {@link Tenant}, or empty if not found.
     */
    Optional<Tenant> findTenantById(Long tenantId);

    /**
     * Finds a tenant name by tenant ID.
     * @param tenantId The ID of the tenant.
     * @return A {@link Optional} containing the name of tenant
     */
    Optional<String> findTenantNameById(Long tenantId);

    /**
     * Finds a user by their ID.
     * @param userId The ID of the user to find.
     * @return An {@link Optional} containing the {@link com.survey_engine.user.models.User}, or empty if not found.
     */
    Optional<User> findUserById(String userId);

    /**
     * Finds essential user details by their ID and returns them as a Map.
     * The map will contain keys such as "name", "email", and "phone".
     * @param userId The ID of the user to find.
     * @return A {@link Map} containing user details, or an empty map if the user is not found.
     */
    Map<String, String> findUserDetailsMapById(String userId);

    /**
     * Finds all users for a given list of user IDs.
     * @param userIds The list of user IDs to find.
     * @return A list of {@link com.survey_engine.user.models.User}.
     */
    List<User> findUsersByIds(List<String> userIds);

    /**
     * Finds all users for a given tenant and department.
     * @param tenantId The ID of the tenant.
     * @param department The department of the users.
     * @return A list of {@link com.survey_engine.user.models.User}.
     */
    List<User> findUsersByTenantIdAndDepartment(Long tenantId, String department);

    /**
     * Finds a user by their subject (from JWT).
     * @param subject The subject of the user to find.
     * @return An {@link Optional} containing the {@link com.survey_engine.user.models.User}, or empty if not found.
     */
    Optional<User> findUserBySubject(String subject);

    /**
     * Finds a tenant ID by user email.
     * @param email The email of the user.
     * @return An {@link Optional} containing the tenant ID, or empty if not found.
     */
    Optional<Long> findTenantIdByEmail(String email);

    /**
     * Finds a user ID by user email.
     * @param email The email of the user.
     * @return An {@link Optional} containing the user ID, or empty if not found.
     */
    Optional<Long> findUserIdByEmail(String email);

    /**
     * Returns the name of a user given their ID.
     * @param userId The ID of the user.
     * @return The name of the user.
     * @throws EntityNotFoundException if the user is not found.
     */
    String getUserNameById(String userId);

    /**
     * Returns a map of user IDs to their names for a given list of user IDs.
     * @param userIds The list of user IDs.
     * @return A map where keys are user IDs (String) and values are usernames (String).
     */
    Map<String, String> getUserNamesByIds(Set<String> userIds);

    /**
     * Returns the department of a user given their ID.
     * @param userId The ID of the user.
     * @return The department of the user.
     * @throws EntityNotFoundException if the user is not found.
     */
    String getUserDepartmentById(String userId);

    /**
     * Returns a list of user IDs for a given tenant and department.
     * @param tenantId The ID of the tenant.
     * @param department The department name.
     * @return A list of user IDs (String).
     */
    List<String> getUserIdsByTenantIdAndDepartment(Long tenantId, String department);

    /**
     * Returns a list of user IDs based on the hierarchical scope.
     *
     * @param tenantId   The ID of the tenant.
     * @param department The department name (nullable).
     * @param region     The region name (nullable).
     * @param branch     The branch name (nullable).
     * @return A list of user IDs (String) matching the scope.
     */
    List<String> findUserIdsByScope(Long tenantId, String department, String region, String branch);

    /**
     * Updates the subscription ID for a specific user.
     * @param userId The ID of the user.
     * @param subscriptionId The UUID of the subscription.
     */
    void updateUserSubscriptionId(Long userId, UUID subscriptionId);

    /**
     * Updates the subscription ID for a specific tenant.
     * @param tenantId The ID of the tenant.
     * @param subscriptionId The UUID of the subscription.
     */
    void updateTenantSubscriptionId(Long tenantId, UUID subscriptionId);

    /**
     * Retrieves the subscription ID for a specific user.
     * @param userId The ID of the user.
     * @return An {@link Optional} containing the subscription UUID, or empty if not set.
     */
    Optional<UUID> getUserSubscriptionId(Long userId);

    /**
     * Retrieves the subscription ID for a specific tenant.
     * @param tenantId The ID of the tenant.
     * @return An {@link Optional} containing the subscription UUID, or empty if not set.
     */
    Optional<UUID> getTenantSubscriptionId(Long tenantId);

    /**
     * Upgrades an individual user to an enterprise tenant using the provided business name.
     * Creates a new tenant and assigns the user as ADMIN.
     *
     * @param userId The ID of the user.
     * @param businessName The name of the new business/tenant.
     * @return The ID of the newly created tenant.
     */
    Long upgradeUserToEnterprise(Long userId, String businessName);

    /**
     * Manually sets the tenant context for the current thread.
     * Use with caution, typically for context switching during upgrades or background tasks.
     * @param tenantId The tenant ID to set.
     */
    void setTenantId(Long tenantId);
}