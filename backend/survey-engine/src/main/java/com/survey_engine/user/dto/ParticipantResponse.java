package com.survey_engine.user.dto;

public record ParticipantResponse(
        Long id,
        String firstName,
        String lastName,
        String phoneNumber,
        String email,
        Long companyId
) {}