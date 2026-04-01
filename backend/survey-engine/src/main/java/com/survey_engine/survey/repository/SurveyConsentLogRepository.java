package com.survey_engine.survey.repository;

import com.survey_engine.survey.models.SurveyConsentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SurveyConsentLogRepository extends JpaRepository<SurveyConsentLog, UUID> {

    boolean existsBySurveyIdAndPhoneHashAndEventType(Long surveyId, String phoneHash, String eventType);

    boolean existsBySurveyIdAndParticipantIdAndEventType(Long surveyId, String participantId, String eventType);
}
