package com.survey_engine.performance_survey.dto;

import com.survey_engine.performance_survey.models.scoring.enums.ScoringStrategy;

import java.util.Map;
import java.util.UUID;

public record QuestionScoringRuleResponse(
        UUID id,
        Long questionId,
        Double weight,
        ScoringStrategy scoringStrategy,
        Map<String, Double> optionScoreMap
) {}