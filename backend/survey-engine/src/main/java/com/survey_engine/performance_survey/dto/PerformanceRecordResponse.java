package com.survey_engine.performance_survey.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PerformanceRecordResponse(
        UUID id,
        Long surveyId,
        Long responseId,
        UUID subjectId,
        String subjectUserId,
        String subjectReferenceCode,
        String evaluatorUserId,
        Double rawScore,
        Double normalizedScore,
        UUID orgUnitId,
        LocalDateTime recordedAt
) {}
