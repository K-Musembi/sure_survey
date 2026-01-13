package com.survey_engine.performance_survey.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SurveyScoringSchemaRequest(
        @NotNull Long surveyId,
        Double defaultQuestionWeight,
        Double targetScore,
        List<QuestionScoringRuleRequest> rules
) {}