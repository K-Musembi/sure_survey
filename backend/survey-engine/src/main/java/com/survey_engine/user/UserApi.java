package com.survey_engine.user;

import com.survey_engine.user.models.Tenant;
import org.springframework.modulith.NamedInterface;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

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
     * Finds user details by email.
     * @param email The email of the user.
     * @return An {@link Optional} containing {@link UserDetails}, or empty if not found.
     */
    Optional<UserDetails> findUserDetailsByEmail(String email);

    /**
     * Retrieves the tenant ID from the current context.
     * @return The ID of the current tenant.
     */
    Long getTenantId();

    /**
     * Finds a {@link com.survey_engine.user.models.Tenant} by its ID.
     * @param tenantId The ID of the tenant to find.
     * @return An {@link Optional} containing the {@link com.survey_engine.user.models.Tenant}, or empty if not found.
     */
    Optional<Tenant> findTenantById(Long tenantId);
}