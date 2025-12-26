package com.survey_engine.user.service;

import com.survey_engine.billing.BillingApi;
import com.survey_engine.common.models.SystemSetting;
import com.survey_engine.common.repository.SystemSettingRepository;
import com.survey_engine.survey.SurveyApi;
import com.survey_engine.user.dto.*;
import com.survey_engine.user.models.Tenant;
import com.survey_engine.user.models.User;
import com.survey_engine.user.repository.TenantRepository;
import com.survey_engine.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service layer for SUPER_ADMIN functionalities.
 * This service provides methods for system-wide administration, bypassing tenant scoping.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final SurveyApi surveyApi;
    private final SystemSettingRepository systemSettingRepository;
    private final BillingApi billingApi;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new system administrator (SUPER_ADMIN).
     *
     * @param request The sign-up request details.
     * @return A UserResponse containing the new admin's details.
     */
    @Transactional
    public UserResponse registerSystemAdmin(SignUpRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("SUPER_ADMIN");
        user.setTenantId(null); // System admins do not belong to a specific tenant

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getDepartment(),
                savedUser.getRegion(),
                savedUser.getBranch(),
                null,
                "SYSTEM"
        );
    }

    /**
     * @param planId The ID of the plan to update.
     * @param price The new price of the plan.
     * @param features The new features of the plan.
     */
    @Transactional
    public void updatePlan(Long planId, BigDecimal price, Map<String, Object> features) {
        billingApi.updatePlan(planId, price, features);
    }

    /**
     * Restocks the system inventory from an external provider.
     */
    @Transactional
    public void restockSystemWallet(String walletType, BigDecimal amount) {
        billingApi.restockSystemWallet(walletType, amount);
    }

    /**
     * Retrieves a list of all tenants in the system with detailed information, including user counts.
     * This operation is not scoped by tenant and is intended for SUPER_ADMIN use only.
     *
     * @return A list of {@link TenantDetailsResponse} objects.
     */
    @Transactional(readOnly = true)
    public List<TenantDetailsResponse> getAllTenantsWithDetails() {
        List<Tenant> tenants = tenantRepository.findAll();
        List<TenantUserCount> userCounts = userRepository.countUsersByTenant();

        Map<Long, Long> userCountMap = userCounts.stream()
                .collect(Collectors.toMap(TenantUserCount::tenantId, TenantUserCount::userCount));

        return tenants.stream()
                .map(tenant -> {
                    long count = userCountMap.getOrDefault(tenant.getId(), 0L);
                    return TenantDetailsResponse.from(tenant, count);
                })
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all surveys belonging to a specific tenant by calling the Survey module's public API
     * and then maps the results to DTOs.
     * This operation is intended for SUPER_ADMIN use only.
     *
     * @param tenantId The ID of the tenant whose surveys are to be retrieved.
     * @return A list of {@link TenantSurveysResponse} objects.
     */
    @Transactional(readOnly = true)
    public List<TenantSurveysResponse> getSurveysForTenant(Long tenantId) {
        List<Map<String, Object>> surveysData = surveyApi.findSurveysByTenantId(tenantId);

        if (surveysData.isEmpty()) {
            return List.of();
        }
        return mapToTenantSurveysResponse(surveysData);
    }

    /**
     * Retrieves all system configuration settings.
     *
     * @return A list of {@link SystemSettingResponse}.
     */
    @Transactional(readOnly = true)
    public List<SystemSettingResponse> getAllSystemSettings() {
        return systemSettingRepository.findAll().stream()
                .map(s -> new SystemSettingResponse(s.getKey(), s.getValue(), s.getDescription()))
                .collect(Collectors.toList());
    }

    /**
     * Updates a list of system settings. If a setting does not exist, it is created.
     *
     * @param requests A list of {@link SystemSettingRequest} containing keys and new values.
     * @return A list of updated {@link SystemSettingResponse}.
     */
    @Transactional
    public List<SystemSettingResponse> updateSystemSettings(List<SystemSettingRequest> requests) {
        List<SystemSetting> settingsToSave = new ArrayList<>();

        for (SystemSettingRequest request : requests) {
            SystemSetting setting = systemSettingRepository.findByKey(request.key())
                    .orElseGet(() -> {
                        SystemSetting newSetting = new SystemSetting();
                        newSetting.setKey(request.key());
                        return newSetting;
                    });
            setting.setValue(request.value());
            settingsToSave.add(setting);
        }

        return systemSettingRepository.saveAll(settingsToSave).stream()
                .map(s -> new SystemSettingResponse(s.getKey(), s.getValue(), s.getDescription()))
                .collect(Collectors.toList());
    }

    /**
     * Maps a list of raw survey data maps to a list of TenantSurveysResponse DTOs.
     *
     * @param surveysData The list of maps, where each map represents a survey's data.
     * @return A list of {@link TenantSurveysResponse} DTOs.
     */
    private List<TenantSurveysResponse> mapToTenantSurveysResponse(List<Map<String, Object>> surveysData) {
        return surveysData.stream()
                .map(data -> new TenantSurveysResponse(
                        ((Number) data.get("id")).longValue(),
                        (String) data.get("name"),
                        (String) data.get("introduction"),
                        (String) data.get("type"),
                        Long.parseLong((String) data.get("userId")),
                        (String) data.get("status"),
                        (String) data.get("accessType"),
                        (LocalDateTime) data.get("startDate"),
                        (LocalDateTime) data.get("endDate"),
                        (LocalDateTime) data.get("createdAt")
                ))
                .collect(Collectors.toList());
    }
}
