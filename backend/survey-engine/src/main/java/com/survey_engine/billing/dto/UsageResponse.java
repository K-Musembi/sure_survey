package com.survey_engine.billing.dto;

import java.util.List;

/**
 * Response DTO for the usage stats endpoint.
 * Provides current usage counts and plan limits so the frontend can render
 * usage banners, upgrade prompts, and feature gates.
 */
public record UsageResponse(
        String planName,             // "Free", "Basic", "Pro", "Enterprise" (null subscription = "Free")
        int currentSurveys,
        int maxSurveys,              // -1 = unlimited
        List<String> allowedChannels,
        boolean aiAnalysis,
        boolean referralEngine,
        boolean performanceSurvey,
        boolean rewards,
        boolean webhooks,
        boolean businessIntelligence
) {}
