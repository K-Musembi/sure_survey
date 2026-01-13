package com.survey_engine.performance_survey.events;

import java.util.UUID;

public record ScoreCalculatedEvent(
        UUID performanceRecordId,
        String subjectUserId,
        Double normalizedScore,
        UUID orgUnitId
) {}