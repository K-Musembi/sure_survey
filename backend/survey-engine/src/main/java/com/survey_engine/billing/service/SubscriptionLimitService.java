package com.survey_engine.billing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey_engine.billing.dto.PlanFeatures;
import com.survey_engine.billing.models.Subscription;
import com.survey_engine.billing.models.enums.SubscriptionStatus;
import com.survey_engine.billing.repository.SubscriptionRepository;
import com.survey_engine.survey.SurveyApi;
import com.survey_engine.survey.common.enums.SurveyType;
import com.survey_engine.user.UserApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to validate if a tenant is allowed to perform actions based on their subscription plan.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionLimitService {

    private final SubscriptionRepository subscriptionRepository;
    private final SurveyApi surveyApi; // To check current usage
    private final UserApi userApi;
    private final ObjectMapper objectMapper;

    /**
     * Checks if the tenant can create a new survey.
     */
    @Transactional(readOnly = true)
    public void validateSurveyCreationLimit(Long tenantId) {
        // 1. Get Active Subscription
        Subscription subscription = getActiveSubscription(tenantId);
        if (subscription == null) {
            // Fallback for FREE tier or no sub? 
            // Assuming logic: No sub = Free Tier defaults
            validateFreeTierSurveyLimit(tenantId);
            return;
        }

        // 2. Parse Features
        PlanFeatures features = parseFeatures(subscription.getPlan().getFeatures());
        if (features == null || features.maxSurveys() == null) {
            return; // No limit defined
        }

        // 3. Check Usage
        long currentSurveyCount = surveyApi.findSurveysByTenantId(tenantId).size();
        if (currentSurveyCount >= features.maxSurveys()) {
            throw new IllegalStateException("Subscription limit reached. You can only create " + features.maxSurveys() + " surveys on this plan.");
        }
    }

    /**
     * Checks if a survey can accept a new response.
     */
    @Transactional(readOnly = true)
    public void validateResponseLimit(Long tenantId, Long surveyId) {
        Subscription subscription = getActiveSubscription(tenantId);
        if (subscription == null) {
            validateFreeTierResponseLimit(surveyId);
            return;
        }

        PlanFeatures features = parseFeatures(subscription.getPlan().getFeatures());
        if (features == null || features.maxResponsesPerSurvey() == null) {
            return; // No limit
        }

        long currentResponses = surveyApi.getResponseRepository().countBySurveyId(surveyId);
        if (currentResponses >= features.maxResponsesPerSurvey()) {
            throw new IllegalStateException("This survey has reached the maximum response limit for your subscription plan.");
        }
    }

    private Subscription getActiveSubscription(Long tenantId) {
        // Use existing repo method or similar logic
        return subscriptionRepository.findFirstByTenantIdAndStatusOrderByIdAsc(tenantId, SubscriptionStatus.ACTIVE)
                .orElse(null);
    }

    private void validateFreeTierSurveyLimit(Long tenantId) {
        // Hardcoded default for tenants without a plan (Implicit Free)
        long count = surveyApi.findSurveysByTenantId(tenantId).size();
        if (count >= 5) { // Example limit
            throw new IllegalStateException("Free limit reached. Please upgrade to create more surveys.");
        }
    }

    private void validateFreeTierResponseLimit(Long surveyId) {
        long count = surveyApi.getResponseRepository().countBySurveyId(surveyId);
        if (count >= 25) {
            throw new IllegalStateException("Free survey response limit (25) reached.");
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