package com.survey_engine.performance_survey.dto;

import com.survey_engine.performance_survey.models.structure.enums.OrgRole;
import com.survey_engine.performance_survey.models.structure.enums.SubjectType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PerformanceSubjectRequest(
        String userId,
        @NotBlank String referenceCode,
        @NotBlank String displayName,
        @NotNull SubjectType type,
        @NotNull UUID orgUnitId,
        @NotNull OrgRole role
) {}
