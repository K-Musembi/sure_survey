package com.survey_engine.billing.dto;

import java.util.List;

/**
 * Strictly typed representation of the JSON stored in Plan.features.
 * New fields default to null if absent from stored JSON (Jackson ignores unknowns).
 */
public record PlanFeatures(
        Integer maxSurveys,                  // -1 = unlimited
        Integer maxResponsesPerSurvey,       // -1 = unlimited
        List<String> channels,               // e.g., ["WEB", "SMS"]
        Boolean aiAnalysis,
        Boolean referralEngine,
        Boolean performanceSurvey,
        Boolean webhooks,
        Boolean isCustomPricing,             // true = Enterprise / contact sales
        List<String> displayFeatures         // human-readable feature list for UI
) {}
