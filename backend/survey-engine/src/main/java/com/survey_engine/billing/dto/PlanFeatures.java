package com.survey_engine.billing.dto;

import java.util.List;

/**
 * Strictly typed representation of the JSON stored in Plan.features.
 */
public record PlanFeatures(
        Integer maxSurveys,
        Integer maxResponsesPerSurvey,
        List<String> channels // e.g., ["WEB", "SMS"]
) {}