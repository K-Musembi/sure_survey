package com.survey_engine.survey.dto;

import com.survey_engine.survey.models.BranchRule;
import com.survey_engine.survey.common.enums.ConditionType;

public record BranchRuleResponse(
        Long id,
        Long surveyId,
        Long sourceQuestionId,
        ConditionType conditionType,
        String conditionValue,
        Long targetQuestionId,
        int priority,
        boolean active
) {
    public static BranchRuleResponse from(BranchRule rule) {
        return new BranchRuleResponse(
                rule.getId(),
                rule.getSurvey().getId(),
                rule.getSourceQuestionId(),
                rule.getConditionType(),
                rule.getConditionValue(),
                rule.getTargetQuestionId(),
                rule.getPriority(),
                rule.isActive()
        );
    }
}
