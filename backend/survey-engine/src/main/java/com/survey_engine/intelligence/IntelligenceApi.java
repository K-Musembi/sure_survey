package com.survey_engine.intelligence;

import org.springframework.modulith.NamedInterface;

import java.util.UUID;

/**
 * Public API for the intelligence module.
 */
@NamedInterface("intelligence")
public interface IntelligenceApi {

    /**
     * Request generation of an insight report for a survey.
     * Generation is async — the caller receives a report ID to poll.
     */
    UUID requestReportForSurvey(Long tenantId, Long surveyId, String sector);
}
