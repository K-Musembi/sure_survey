package com.survey_engine.survey.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DistributionListResponse(
        UUID id,
        String name,
        List<ContactResponse> contacts,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
