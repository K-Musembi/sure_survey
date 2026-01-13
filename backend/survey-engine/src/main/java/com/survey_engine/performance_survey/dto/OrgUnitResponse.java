package com.survey_engine.performance_survey.dto;

import com.survey_engine.performance_survey.models.structure.enums.OrgUnitType;

import java.util.UUID;

public record OrgUnitResponse(
        UUID id,
        String name,
        OrgUnitType type,
        UUID parentId,
        String managerId
) {}