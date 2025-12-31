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
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

import com.survey_engine.user.dto.TenantRequest;
import com.survey_engine.user.service.TenantService;

import com.survey_engine.user.service.TenantContext;

@Service
@RequiredArgsConstructor
public class UserApiImpl implements UserApi {

    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final TenantService tenantService;

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

    /**
     * Finds tenant name by tenant ID.
     * @param tenantId The ID of the user to find.
     * @return A {@link Optional} containing the name of the tenant
     */
    @Override
    public Optional<String> findTenantNameById(Long tenantId) {
        return tenantRepository.findById(tenantId).map(Tenant::getName);
    }

    @Override
    public Optional<User> findUserById(String userId) {
        if (userId.matches("\\d+")) {
            return userRepository.findById(Long.parseLong(userId));
        }
        return userRepository.findByEmail(userId);
    }

    @Override
    public Map<String, String> findUserDetailsMapById(String userId) {
        Optional<User> userOptional;
        if (userId.matches("\\d+")) {
            userOptional = userRepository.findById(Long.parseLong(userId));
        } else {
            userOptional = userRepository.findByEmail(userId);
        }

        return userOptional
                .map(user -> {
                    Map<String, String> userDetails = new HashMap<>();
                    userDetails.put("name", user.getName());
                    userDetails.put("email", user.getEmail());
                    // TODO: The User entity does not have a phone number. Using email as a placeholder.
                    userDetails.put("phone", user.getEmail());
                    return userDetails;
                })
                .orElse(Collections.emptyMap());
    }

    @Override
    public List<User> findUsersByIds(List<String> userIds) {
        List<Long> longUserIds = userIds.stream()
                .filter(s -> s.matches("\\d+"))
                .map(Long::parseLong).collect(toList());
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

    /**
     * Finds a tenant ID by user email.
     * @param email The email of the user.
     * @return An {@link Optional} containing the tenant ID, or empty if not found.
     */
    @Override
    public Optional<Long> findTenantIdByEmail(String email) {
        return userRepository.findByEmail(email).map(User::getTenantId);
    }

    /**
     * Finds a user ID by user email.
     * @param email The email of the user.
     * @return An {@link Optional} containing the user ID, or empty if not found.
     */
    @Override
    public Optional<Long> findUserIdByEmail(String email) {
        return userRepository.findByEmail(email).map(User::getId);
    }

    @Override
    public String getUserNameById(String userId) {
        Optional<User> userOptional;
        if (userId.matches("\\d+")) {
            userOptional = userRepository.findById(Long.parseLong(userId));
        } else {
            userOptional = userRepository.findByEmail(userId);
        }
        return userOptional
                .map(User::getName)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    @Override
    public Map<String, String> getUserNamesByIds(Set<String> userIds) {
        List<Long> longUserIds = userIds.stream()
                .filter(s -> s.matches("\\d+"))
                .map(Long::parseLong).collect(toList());
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

    @Override
    public List<String> findUserIdsByScope(Long tenantId, String department, String region, String branch) {
        List<User> users;
        if (branch != null) {
            users = userRepository.findByTenantIdAndDepartmentAndRegionAndBranch(tenantId, department, region, branch);
        } else if (region != null) {
            users = userRepository.findByTenantIdAndDepartmentAndRegion(tenantId, department, region);
        } else if (department != null) {
            users = userRepository.findByTenantIdAndDepartment(tenantId, department);
        } else {
            users = userRepository.findByTenantId(tenantId);
        }
        return users.stream()
                .map(user -> String.valueOf(user.getId()))
                .collect(toList());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void updateUserSubscriptionId(Long userId, java.util.UUID subscriptionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        user.setSubscriptionId(subscriptionId);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateTenantSubscriptionId(Long tenantId, UUID subscriptionId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with id: " + tenantId));
        tenant.setSubscriptionId(subscriptionId);
        tenantRepository.save(tenant);
    }

    @Override
    public Optional<UUID> getUserSubscriptionId(Long userId) {
        return userRepository.findById(userId).map(User::getSubscriptionId);
    }

    @Override
    public Optional<UUID> getTenantSubscriptionId(Long tenantId) {
        return tenantRepository.findById(tenantId).map(Tenant::getSubscriptionId);
    }

    @Override
    @Transactional
    public Long upgradeUserToEnterprise(Long userId, String businessName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // Create new Tenant
        TenantRequest request = new TenantRequest(businessName, businessName.toLowerCase().replaceAll("\\s+", "-"));
        Tenant newTenant = tenantService.createTenant(request);

        // Update User
        user.setTenantId(newTenant.getId());
        user.setTenant(newTenant);
        user.setRole("ADMIN"); // Promote to Admin of new tenant
        userRepository.save(user);

        return newTenant.getId();
    }

    @Override
    public void setTenantId(Long tenantId) {
        TenantContext.setTenantId(tenantId);
    }

}