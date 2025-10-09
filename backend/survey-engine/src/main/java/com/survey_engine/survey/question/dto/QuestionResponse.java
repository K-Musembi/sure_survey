package com.survey.survey.question.dto;

import com.survey.survey.common.enums.QuestionType;

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