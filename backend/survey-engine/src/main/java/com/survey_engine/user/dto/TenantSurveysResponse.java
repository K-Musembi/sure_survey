package com.survey_engine.user.dto;

import java.time.LocalDateTime;

public record TenantSurveysResponse(
        Long id,
        String name,
        String introduction,
        String type,
        Long userId,
        String status,
        String accessType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        LocalDateTime createdAt
) {
}
