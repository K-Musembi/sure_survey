package com.survey_engine.survey.dto;

public record ContactResponse(
        String phoneNumber,
        String firstName,
        String lastName,
        String email
) {
}
