package com.survey_engine.intelligence.dto;

import com.survey_engine.intelligence.domain.enums.PlanStatus;

import java.time.LocalDate;

public record UpdateActionPlanRequest(
        PlanStatus status,
        String completionNotes,
        LocalDate dueDate
) {}
