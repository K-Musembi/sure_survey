package com.survey_engine.ai_analysis.tools;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.survey_engine.survey.SurveyApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.function.Function;

@Configuration
public class SurveyDataTools {

    private static final Logger logger = LoggerFactory.getLogger(SurveyDataTools.class);
    private final SurveyApi surveyApi;

    public SurveyDataTools(SurveyApi surveyApi) {
        this.surveyApi = surveyApi;
    }

    @Bean
    @Description("Fetches all text responses for a specific survey ID. Use this to analyze sentiment or summarize feedback.")
    public Function<SurveyIdRequest, List<String>> fetchSurveyResponses() {
        return request -> {
            logger.info("Tool called: fetchSurveyResponses for surveyId {}", request.surveyId());
            return surveyApi.getSurveyResponseTexts(request.surveyId());
        };
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("Request containing the Survey ID to fetch responses for.")
    public record SurveyIdRequest(
            @JsonProperty(required = true, value = "surveyId")
            @JsonPropertyDescription("The unique identifier of the survey")
            Long surveyId
    ) {}
}