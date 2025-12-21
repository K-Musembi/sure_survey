package com.survey_engine.ai_analysis.dto;

public record SurveyGenerationRequest(
    String topic,
    String type,
    String sector,
    Integer questionCount
) {}