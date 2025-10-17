package com.survey_engine.user.dto;

public record ParticipantResponse(
        Long id,
        String fullName,
        String phoneNumber,
        String email
) {}