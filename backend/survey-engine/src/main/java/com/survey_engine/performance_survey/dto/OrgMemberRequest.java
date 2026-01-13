package com.survey_engine.performance_survey.dto;

import com.survey_engine.performance_survey.models.structure.enums.OrgRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrgMemberRequest(
        @NotBlank String userId,
        @NotNull UUID orgUnitId,
        @NotNull OrgRole role
) {}