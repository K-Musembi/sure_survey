package com.survey_engine.user.service;

import com.survey_engine.common.events.ResponseParticipantEvent;
import com.survey_engine.common.events.ResponseParticipantCheckEvent;
import com.survey_engine.user.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipantCheckListener {

    private final ParticipantRepository participantRepository;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    public void handleResponseNeedingParticipant(ResponseParticipantEvent event) {
        log.info("Received request to find participant for responseId: {} with phone number: {}", event.responseId(), event.phoneNumber());

        participantRepository.findByPhoneNumber(event.phoneNumber()).ifPresent(participant -> {
            log.info("Found participantId: {} for phone number: {}. Publishing result.", participant.getId(), event.phoneNumber());

            ResponseParticipantCheckEvent resultEvent = new ResponseParticipantCheckEvent(
                    event.responseId(),
                    String.valueOf(participant.getId())
            );
            eventPublisher.publishEvent(resultEvent);
        });
    }
}

