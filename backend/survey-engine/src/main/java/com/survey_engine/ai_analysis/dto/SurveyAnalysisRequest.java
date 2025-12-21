package com.survey_engine.ai_analysis.dto;

public record SurveyAnalysisRequest(
    Long surveyId,
    String query
) {}
