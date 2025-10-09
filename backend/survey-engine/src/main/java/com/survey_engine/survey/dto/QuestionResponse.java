package com.survey_engine.survey.dto;

import com.survey_engine.survey.common.enums.QuestionType;

/**
 * Response Data Transfer Object (DTO) for Question entity
 * @param id
 * @param questionText
 * @param questionType
 * @param options
 * @param position
 */
public record QuestionResponse(
        Long id,
        String questionText,
        QuestionType questionType,
        String options,
        Integer position
) {
}