package com.survey_engine.performance_survey.dto;

import java.util.List;
import java.util.UUID;

public record SurveyScoringSchemaResponse(
        UUID id,
        Long surveyId,
        Double defaultQuestionWeight,
        Double targetScore,
        List<QuestionScoringRuleResponse> rules
) {}