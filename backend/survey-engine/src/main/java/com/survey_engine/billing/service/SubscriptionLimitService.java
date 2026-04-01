package com.survey_engine.billing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey_engine.billing.dto.PlanFeatures;
import com.survey_engine.billing.dto.UsageResponse;
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

import java.util.List;
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

        long currentSurveyCount = countUserSurveys(tenantId, userId);

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
        Long userId = parseLongSafe(ownerUserIdObj);

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

        long currentCount = surveyApi.countResponsesBySurveyId(surveyId);
        if (currentCount >= features.maxResponsesPerSurvey()) {
            throw new BusinessRuleException(
                    "RESPONSE_LIMIT_REACHED",
                    "Response limit of " + features.maxResponsesPerSurvey() + " reached for this survey. Please upgrade your plan."
            );
        }
    }

    /**
     * Checks if the given channel is allowed by the tenant's subscription plan.
     * Free tier and Basic allow WEB only. Pro/Enterprise allow WEB, SMS, WHATSAPP.
     */
    @Transactional(readOnly = true)
    public void validateChannelAllowed(Long tenantId, Long userId, String channel) {
        if (channel == null || "WEB".equalsIgnoreCase(channel)) {
            return; // WEB is always allowed
        }

        Subscription subscription = subscriptionService.getActiveSubscriptionForUser(tenantId, userId)
                .orElse(null);

        if (subscription == null) {
            // Free tier — WEB only
            throw new BusinessRuleException(
                    "CHANNEL_NOT_ALLOWED",
                    "The " + channel + " channel is not available on the free tier. Please upgrade your plan."
            );
        }

        PlanFeatures features = parseFeatures(subscription.getPlan().getFeatures());
        if (features == null || features.channels() == null) {
            return; // No channel restrictions defined
        }

        boolean allowed = features.channels().stream()
                .anyMatch(c -> c.equalsIgnoreCase(channel));

        if (!allowed) {
            throw new BusinessRuleException(
                    "CHANNEL_NOT_ALLOWED",
                    "The " + channel + " channel is not available on your current plan. Please upgrade to access this channel."
            );
        }
    }

    /**
     * Returns current usage stats and plan limits for the authenticated user.
     */
    @Transactional(readOnly = true)
    public UsageResponse getUsage(Long tenantId, Long userId) {
        int currentSurveys = countUserSurveys(tenantId, userId);

        Subscription subscription = subscriptionService.getActiveSubscriptionForUser(tenantId, userId)
                .orElse(null);

        if (subscription == null) {
            // Free tier defaults
            return new UsageResponse("Free", currentSurveys, 3,
                    List.of("WEB"), false, false, false, false, false, false);
        }

        PlanFeatures f = parseFeatures(subscription.getPlan().getFeatures());
        if (f == null) {
            return new UsageResponse(subscription.getPlan().getName(), currentSurveys, -1,
                    List.of("WEB"), false, false, false, false, false, false);
        }

        return new UsageResponse(
                subscription.getPlan().getName(),
                currentSurveys,
                f.maxSurveys() != null ? f.maxSurveys() : -1,
                f.channels() != null ? f.channels() : List.of("WEB"),
                Boolean.TRUE.equals(f.aiAnalysis()),
                Boolean.TRUE.equals(f.referralEngine()),
                Boolean.TRUE.equals(f.performanceSurvey()),
                Boolean.TRUE.equals(f.rewards()),
                Boolean.TRUE.equals(f.webhooks()),
                Boolean.TRUE.equals(f.businessIntelligence())
        );
    }

    private int countUserSurveys(Long tenantId, Long userId) {
        String tenantName = userApi.findTenantNameById(tenantId).orElse("Main Tenant");
        if (tenantName.equals("Main Tenant")) {
            return (int) surveyApi.countSurveysByUserId(String.valueOf(userId));
        }
        return (int) surveyApi.countSurveysByTenantId(tenantId);
    }

    private void validateFreeTierSurveyLimit(Long tenantId, Long userId) {
        long count = countUserSurveys(tenantId, userId);

        if (count >= 3) {
            throw new BusinessRuleException(
                    "FREE_SURVEY_LIMIT_REACHED",
                    "Free tier limit reached (3 surveys). Please upgrade to create more surveys."
            );
        }
    }

    private void validateFreeTierResponseLimit(Long surveyId) {
        long count = surveyApi.countResponsesBySurveyId(surveyId);
        if (count >= 25) {
            throw new BusinessRuleException(
                    "FREE_RESPONSE_LIMIT_REACHED",
                    "Free tier response limit (25) reached for this survey. Please upgrade your plan."
            );
        }
    }

    private Long parseLongSafe(Object value) {
        if (value == null) return null;
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Could not parse as Long: {}", value);
            return null;
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
