package com.survey_engine.common.events;

public record ResponseNeedsParticipant(
        Long responseId,
        String phoneNumber) {
}
