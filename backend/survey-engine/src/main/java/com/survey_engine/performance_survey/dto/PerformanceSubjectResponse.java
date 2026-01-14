package com.survey_engine.performance_survey.dto;

import com.survey_engine.performance_survey.models.structure.enums.OrgRole;
import com.survey_engine.performance_survey.models.structure.enums.SubjectType;

import java.util.UUID;

public record PerformanceSubjectResponse(
        UUID id,
        String userId,
        String referenceCode,
        String displayName,
        SubjectType type,
        UUID orgUnitId,
        String orgUnitName,
        OrgRole role
) {}
