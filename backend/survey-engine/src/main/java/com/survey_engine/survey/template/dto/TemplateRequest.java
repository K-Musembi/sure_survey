package com.survey_engine.survey.template.dto;

import com.survey_engine.survey.common.enums.SurveyType;
import com.survey_engine.survey.config.security.xss.Sanitize;
import com.survey.survey.template_question.dto.TemplateQuestionRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request DTO for creating a Template.
 * @param name The name of the template.
 * @param type The type of survey this template is for (e.g., NPS, CSAT).
 * @param sector The industry sector this template applies to.
 * @param subSector The specific sub-sector.
 * @param questions A list of questions for the template.
 */
public record TemplateRequest(
        @NotBlank(message = "Template name is required")
        @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
        @Sanitize
        String name,

        @NotNull(message = "Survey type is required")
        SurveyType type,

        @Sanitize
        String sector,

        @Sanitize
        String subSector,

        @Valid
        @NotNull(message = "Questions list cannot be null")
        @Size(min = 1, message = "Template must have at least one question")
        List<TemplateQuestionRequest> questions
) {
}
