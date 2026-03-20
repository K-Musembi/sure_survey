package com.survey_engine.intelligence;

import com.survey_engine.intelligence.dto.GenerateReportRequest;
import com.survey_engine.intelligence.service.InsightGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IntelligenceApiImpl implements IntelligenceApi {

    private final InsightGenerationService insightService;

    @Override
    public UUID requestReportForSurvey(Long tenantId, Long surveyId, String sector) {
        return insightService.requestReport(tenantId,
                new GenerateReportRequest(surveyId, null, sector, null, null));
    }
}
