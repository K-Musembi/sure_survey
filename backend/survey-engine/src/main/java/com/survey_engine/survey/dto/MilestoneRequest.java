package com.survey_engine.survey.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MilestoneRequest(
        @NotNull
        @Min(1)
        @Max(100)
        Integer thresholdPct,

        String message,

        UUID badgeTypeId
) {}
