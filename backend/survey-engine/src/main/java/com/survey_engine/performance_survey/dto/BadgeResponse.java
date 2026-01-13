package com.survey_engine.performance_survey.dto;

import java.util.UUID;

public record BadgeResponse(
        UUID id,
        String name,
        String iconUrl
) {}