package com.survey.survey.survey.dto;

import com.survey.survey.common.enums.AccessType;
import com.survey.survey.common.enums.SurveyStatus;
import com.survey.survey.common.enums.SurveyType;
import com.survey.survey.question.dto.QuestionResponse;

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
        SurveyType type,
        String userId,
        SurveyStatus status,
        AccessType accessType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        LocalDateTime createdAt,
        List<QuestionResponse> questions
) {
}
