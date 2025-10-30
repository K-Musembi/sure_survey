package com.survey_engine.user;

import com.survey_engine.user.models.Participant;
import com.survey_engine.user.models.Tenant;
import com.survey_engine.user.models.User;
import com.survey_engine.user.repository.ParticipantRepository;
import com.survey_engine.user.repository.UserRepository;
import com.survey_engine.user.repository.TenantRepository;
import com.survey_engine.user.service.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

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
     * Retrieves the tenant ID from the current {@link TenantContext}.
     *
     * @return The ID of the current tenant.
     */
    @Override
    public Long getTenantId() {
        return TenantContext.getTenantId();
    }

    /**
     * Finds a {@link Tenant} by its ID.
     *
     * @param tenantId The ID of the tenant to find.
     * @return An {@link Optional} containing the {@link Tenant}, or empty if not found.
     */
    @Override
    public Optional<Tenant> findTenantById(Long tenantId) {
        return tenantRepository.findById(tenantId);
    }

    @Override
    public Optional<User> findUserById(String userId) {
        return userRepository.findById(Long.parseLong(userId));
    }

    @Override
    public List<User> findUsersByIds(List<String> userIds) {
        List<Long> longUserIds = userIds.stream().map(Long::parseLong).collect(toList());
        return userRepository.findAllById(longUserIds);
    }

    @Override
    public List<User> findUsersByTenantIdAndDepartment(Long tenantId, String department) {
        return userRepository.findByTenantIdAndDepartment(tenantId, department);
    }

    @Override
    public Optional<User> findUserBySubject(String subject) {
        return userRepository.findByEmail(subject);
    }

    @Override
    public String getUserNameById(String userId) {
        return userRepository.findById(Long.parseLong(userId))
                .map(User::getName)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    @Override
    public Map<String, String> getUserNamesByIds(Set<String> userIds) {
        List<Long> longUserIds = userIds.stream().map(Long::parseLong).collect(toList());
        return userRepository.findAllById(longUserIds).stream()
                .collect(Collectors.toMap(user -> String.valueOf(user.getId()), User::getName));
    }

    @Override
    public String getUserDepartmentById(String userId) {
        return userRepository.findById(Long.parseLong(userId))
                .map(User::getDepartment)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    @Override
    public List<String> getUserIdsByTenantIdAndDepartment(Long tenantId, String department) {
        return userRepository.findByTenantIdAndDepartment(tenantId, department).stream()
                .map(user -> String.valueOf(user.getId()))
                .collect(toList());
    }

}