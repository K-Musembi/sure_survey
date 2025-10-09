package com.survey_engine.survey.dto;

/**
 * Response DTO for an Answer.
 * @param id The unique identifier of the answer.
 * @param questionId The ID of the question this answer corresponds to.
 * @param answerValue The value of the answer.
 * @param position The position of the question when the answer was submitted.
 */
public record AnswerResponse(
        Long id,
        Long questionId,
        String answerValue,
        Integer position
) {
}
