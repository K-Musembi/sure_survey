package com.survey_engine.user.service;

import com.survey_engine.survey.SurveyApi;
import com.survey_engine.user.dto.TenantDetailsResponse;
import com.survey_engine.user.dto.TenantSurveysResponse;
import com.survey_engine.user.dto.TenantUserCount;
import com.survey_engine.user.models.Tenant;
import com.survey_engine.user.repository.TenantRepository;
import com.survey_engine.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
