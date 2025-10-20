package com.survey_engine.user;

import com.survey_engine.user.models.Participant;
import com.survey_engine.user.repository.ParticipantRepository;
import com.survey_engine.user.repository.UserRepository;
import com.survey_engine.user.repository.TenantRepository;
import com.survey_engine.user.service.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserApiImpl implements UserApi {

    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    /**
     * Finds the participant ID associated with a given phone number.
     *
     * @param phoneNumber The phone number to search for.
     * @return An {@link Optional} containing the participant ID as a String, or empty if not found.
     */
    @Override
    public Optional<String> findParticipantIdByPhoneNumber(String phoneNumber) {
        return participantRepository.findByPhoneNumber(phoneNumber)
                .map(participant -> String.valueOf(participant.getId()));
    }

    /**
     * Finds the phone number associated with a given participant ID.
     *
     * @param participantId The participant ID to search for.
     * @return An {@link Optional} containing the phone number as a String, or empty if not found or invalid ID.
     */
    @Override
    public Optional<String> findPhoneNumberByParticipantId(String participantId) {
        try {
            Long id = Long.parseLong(participantId);
            return participantRepository.findById(id).map(Participant::getPhoneNumber);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Finds user details by email.
     *
     * @param email The email of the user.
     * @return An {@link Optional} containing {@link UserDetails}, or empty if not found.
     */
    @Override
    public Optional<UserDetails> findUserDetailsByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> user);
    }

    /**
     * Retrieves the tenant ID from the current {@link TenantContext}.
     *
     * @return The ID of the current tenant.
     */
    @Override
    public Long getTenantId() {
        return TenantContext.getTenantId();
    }

    /**
     * Finds a {@link com.survey_engine.user.models.Tenant} by its ID.
     *
     * @param tenantId The ID of the tenant to find.
     * @return An {@link Optional} containing the {@link com.survey_engine.user.models.Tenant}, or empty if not found.
     */
    @Override
    public Optional<com.survey_engine.user.models.Tenant> findTenantById(Long tenantId) {
        return tenantRepository.findById(tenantId);
    }

}