package com.survey_engine.performance_survey.dto;

import com.survey_engine.performance_survey.models.scoring.enums.ScoringStrategy;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record QuestionScoringRuleRequest(
        @NotNull Long questionId,
        Double weight,
        @NotNull ScoringStrategy scoringStrategy,
        Map<String, Double> optionScoreMap
) {}