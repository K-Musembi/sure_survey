package com.survey_engine.intelligence.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record GenerateReportRequest(
        @NotNull Long surveyId,
        String title,
        String sector,
        LocalDate periodStart,
        LocalDate periodEnd
) {}
