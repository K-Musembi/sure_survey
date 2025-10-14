package com.survey_engine.common.events;

public record ResponseParticipantCheckEvent(
        Long responseId,
        String participantId) {
}
