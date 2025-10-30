package com.survey_engine.survey.dto;

import com.survey_engine.survey.common.enums.AccessType;
import com.survey_engine.survey.common.enums.SurveyStatus;
import com.survey_engine.survey.common.enums.SurveyType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response Data Transfer Object (DTO) for Survey entity
 * @param id
 * @param name
 * @param type
 * @param userId
 * @param status
 * @param accessType
 * @param startDate
 * @param endDate
 * @param createdAt
 * @param questions
 */
public record SurveyResponse(
        Long id,
        String name,
        String introduction,
        SurveyType type,
        String userId,
        String createdByName,
        SurveyStatus status,
        AccessType accessType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        LocalDateTime createdAt,
        List<QuestionResponse> questions
) {
}
