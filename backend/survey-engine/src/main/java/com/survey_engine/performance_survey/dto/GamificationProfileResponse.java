package com.survey_engine.performance_survey.dto;

import java.util.List;
import java.util.UUID;

public record GamificationProfileResponse(
        UUID id,
        String userId,
        Long totalPoints,
        Integer currentStreak,
        Integer level,
        List<BadgeResponse> badges
) {}