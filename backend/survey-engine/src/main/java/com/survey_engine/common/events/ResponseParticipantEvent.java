package com.survey_engine.common.events;

public record ResponseParticipantEvent(
        Long responseId,
        String phoneNumber) {
}
