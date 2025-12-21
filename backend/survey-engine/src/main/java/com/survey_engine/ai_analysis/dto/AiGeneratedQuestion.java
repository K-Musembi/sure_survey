package com.survey_engine.ai_analysis.dto;

public record AiGeneratedQuestion(
    String questionText,
    String questionType,
    String options,
    Integer position
) {}
