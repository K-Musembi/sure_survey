package com.survey_engine.billing.dto;

import java.util.List;

/**
 * Strictly typed representation of the JSON stored in Plan.features.
 * New fields default to null if absent from stored JSON (Jackson ignores unknowns).
 */
public record PlanFeatures(
        Integer maxSurveys,                  // -1 = unlimited
        Integer maxResponsesPerSurvey,       // -1 = unlimited
        List<String> channels,               // e.g., ["WEB", "SMS", "WHATSAPP"]
        Boolean aiAnalysis,
        Boolean referralEngine,
        Boolean performanceSurvey,
        Boolean rewards,                     // true = can configure survey rewards (airtime, data, loyalty)
        Boolean webhooks,
        Boolean isCustomPricing,             // true = contact sales
        Boolean businessIntelligence,        // true = Enterprise BI dashboards & reports
        List<String> displayFeatures         // human-readable feature list for UI
) {}
