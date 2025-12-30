package com.survey_engine.survey.dto;

import java.util.UUID;

public record SurveyDistributionRequest(
    UUID distributionListId
) {}
