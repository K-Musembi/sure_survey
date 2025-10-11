package com.survey_engine.survey.service;

import com.survey_engine.common.events.ResponseParticipantCheck;
import com.survey_engine.common.events.SurveyCompletedEvent;
import com.survey_engine.survey.models.Response;
import com.survey_engine.survey.repository.ResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipantFoundListener {

    private final ResponseRepository responseRepository;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Transactional
    public void handleParticipantFound(ResponseParticipantCheck event) {
        log.info("Received participant check result for responseId: {}. ParticipantId: {}", event.responseId(), event.participantId());

        if (event.participantId() == null) {
            log.warn("ParticipantId is null for responseId: {}. Cannot enrich response.", event.responseId());
            return;
        }

        responseRepository.findById(event.responseId()).ifPresent(response -> {
            log.info("Found response to enrich. Setting userId to {}", event.participantId());
            response.setUserId(event.participantId());
            Response updatedResponse = responseRepository.save(response);

            // Now that the response is enriched, publish the completion event for other modules
            SurveyCompletedEvent completionEvent = new SurveyCompletedEvent(
                    updatedResponse.getSurvey().getId(),
                    updatedResponse.getId(),
                    updatedResponse.getUserId()
            );
            eventPublisher.publishEvent(completionEvent);
            log.info("Published SurveyCompletedEvent for enriched responseId: {}", updatedResponse.getId());
        });
    }
}

