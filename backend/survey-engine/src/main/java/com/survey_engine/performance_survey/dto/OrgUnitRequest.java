package com.survey_engine.performance_survey.dto;

import com.survey_engine.performance_survey.models.structure.enums.OrgUnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrgUnitRequest(
        @NotBlank String name,
        @NotNull OrgUnitType type,
        UUID parentId,
        String managerId
) {}