package com.survey_engine.common.events;

public record ResponseParticipantCheck(
        Long responseId,
        String participantId) {
}
