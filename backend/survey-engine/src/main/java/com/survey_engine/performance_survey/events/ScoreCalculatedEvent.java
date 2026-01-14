package com.survey_engine.performance_survey.events;

import java.util.UUID;

public record ScoreCalculatedEvent(
        UUID performanceRecordId,
        UUID subjectId,
        String subjectUserId, // Nullable, if the subject is a system user
        Double normalizedScore,
        UUID orgUnitId
) {}
