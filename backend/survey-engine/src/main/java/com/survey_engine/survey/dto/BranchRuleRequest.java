package com.survey_engine.survey.dto;

import com.survey_engine.survey.common.enums.ConditionType;
import jakarta.validation.constraints.NotNull;

public record BranchRuleRequest(
        @NotNull
        Long sourceQuestionId,

        @NotNull
        ConditionType conditionType,

        String conditionValue,          // JSON string; required for all types except ALWAYS

        Long targetQuestionId,          // null = end survey at this branch

        int priority
) {}
