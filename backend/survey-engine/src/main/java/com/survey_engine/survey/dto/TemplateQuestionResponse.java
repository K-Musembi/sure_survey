package com.survey_engine.survey.dto;

import com.survey_engine.survey.common.enums.QuestionType;

/**
 * Response DTO for a TemplateQuestion.
 * @param id The unique identifier of the template question.
 * @param questionText The text of the question.
 * @param questionType The type of the question.
 * @param options The options for the question, as a JSON string.
 * @param position The display order of the question.
 */
public record TemplateQuestionResponse(
        Long id,
        String questionText,
        QuestionType questionType,
        String options,
        Integer position
) {
}
