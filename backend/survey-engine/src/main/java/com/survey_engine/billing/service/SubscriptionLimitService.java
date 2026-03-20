package com.survey_engine.billing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey_engine.billing.dto.PlanFeatures;
import com.survey_engine.billing.models.Subscription;
import com.survey_engine.billing.models.enums.SubscriptionStatus;
import com.survey_engine.billing.repository.SubscriptionRepository;
import com.survey_engine.common.exception.BusinessRuleException;
import com.survey_engine.survey.SurveyApi;
import com.survey_engine.user.UserApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Service to validate if a tenant is allowed to perform actions based on their subscription plan.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionLimitService {

    private final SubscriptionRepository subscriptionRepository;
    private final SurveyApi surveyApi;
    private final UserApi userApi;
    private final ObjectMapper objectMapper;
    private final SubscriptionService subscriptionService;

    /**
     * Checks if the tenant can create a new survey.
     */
    @Transactional(readOnly = true)
    public void validateSurveyCreationLimit(Long tenantId, Long userId) {
        Subscription subscription = subscriptionService.getActiveSubscriptionForUser(tenantId, userId)
                .orElse(null);

        if (subscription == null) {
            validateFreeTierSurveyLimit(tenantId, userId);
            return;
        }

        PlanFeatures features = parseFeatures(subscription.getPlan().getFeatures());
        if (features == null || features.maxSurveys() == null || features.maxSurveys() == -1) {
            return; // No limit or unlimited
        }

        long currentSurveyCount;
        String tenantName = userApi.findTenantNameById(tenantId).orElse("Main Tenant");

        if (tenantName.equals("Main Tenant")) {
            currentSurveyCount = surveyApi.findSurveysByUserId(String.valueOf(userId)).size();
        } else {
            currentSurveyCount = surveyApi.findSurveysByTenantId(tenantId).size();
        }

        if (currentSurveyCount >= features.maxSurveys()) {
            throw new BusinessRuleException(
                    "SURVEY_LIMIT_REACHED",
                    "Survey limit reached. Your plan allows up to " + features.maxSurveys() + " surveys. Please upgrade to create more."
            );
        }
    }

    /**
     * Checks if a survey can accept a new response.
     */
    @Transactional(readOnly = true)
    public void validateResponseLimit(Long tenantId, Long surveyId) {
        // Resolve the survey owner to find their subscription
        Map<String, Object> survey = surveyApi.getSurveyById(surveyId);
        if (survey == null) {
            return; // Survey not found — let it through; controller will handle 404
        }

        Object ownerUserIdObj = survey.get("userId");
        Long userId = ownerUserIdObj != null ? Long.parseLong(ownerUserIdObj.toString()) : null;

        Subscription subscription = subscriptionService.getActiveSubscriptionForUser(tenantId, userId)
                .orElse(null);

        if (subscription == null) {
            validateFreeTierResponseLimit(surveyId);
            return;
        }

        PlanFeatures features = parseFeatures(subscription.getPlan().getFeatures());
        if (features == null || features.maxResponsesPerSurvey() == null || features.maxResponsesPerSurvey() == -1) {
            return; // No limit or unlimited
        }

        long currentCount = surveyApi.getResponseRepository().countBySurveyId(surveyId);
        if (currentCount >= features.maxResponsesPerSurvey()) {
            throw new BusinessRuleException(
                    "RESPONSE_LIMIT_REACHED",
                    "Response limit of " + features.maxResponsesPerSurvey() + " reached for this survey. Please upgrade your plan."
            );
        }
    }

    private void validateFreeTierSurveyLimit(Long tenantId, Long userId) {
        long count;
        String tenantName = userApi.findTenantNameById(tenantId).orElse("Main Tenant");

        if (tenantName.equals("Main Tenant")) {
            count = surveyApi.findSurveysByUserId(String.valueOf(userId)).size();
        } else {
            count = surveyApi.findSurveysByTenantId(tenantId).size();
        }

        if (count >= 5) {
            throw new BusinessRuleException(
                    "FREE_SURVEY_LIMIT_REACHED",
                    "Free tier limit reached (5 surveys). Please upgrade to create more surveys."
            );
        }
    }

    private void validateFreeTierResponseLimit(Long surveyId) {
        long count = surveyApi.getResponseRepository().countBySurveyId(surveyId);
        if (count >= 25) {
            throw new BusinessRuleException(
                    "FREE_RESPONSE_LIMIT_REACHED",
                    "Free tier response limit (25) reached for this survey. Please upgrade your plan."
            );
        }
    }

    private PlanFeatures parseFeatures(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, PlanFeatures.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse plan features JSON: {}", json, e);
            return null;
        }
    }
}
