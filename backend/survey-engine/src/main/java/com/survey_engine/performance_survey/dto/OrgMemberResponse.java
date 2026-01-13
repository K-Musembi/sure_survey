package com.survey_engine.performance_survey.dto;

import com.survey_engine.performance_survey.models.structure.enums.OrgRole;

import java.util.UUID;

public record OrgMemberResponse(
        UUID id,
        String userId,
        UUID orgUnitId,
        String orgUnitName,
        OrgRole role
) {}