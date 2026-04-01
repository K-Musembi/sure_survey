package com.survey_engine.ai_analysis.dto;

import java.util.List;

public record AiGeneratedQuestion(
    String questionText,
    String questionType,
    List<String> options,
    Integer position
) {}
