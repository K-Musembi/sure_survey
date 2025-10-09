package com.survey.survey.template.dto;

import com.survey.survey.common.enums.SurveyType;
import com.survey.survey.template_question.dto.TemplateQuestionResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for a Template.
 * @param id The unique identifier of the template.
 * @param name The name of the template.
 * @param type The type of survey the template is for.
 * @param sector The industry sector.
 * @param subSector The specific sub-sector.
 * @param questions The list of questions in the template.
 * @param createdAt The date and time the template was created.
 */
public record TemplateResponse(
        Long id,
        String name,
        SurveyType type,
        String sector,
        String subSector,
        List<TemplateQuestionResponse> questions,
        LocalDateTime createdAt
) {
}
