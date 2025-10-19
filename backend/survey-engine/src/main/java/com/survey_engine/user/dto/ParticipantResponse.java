package com.survey_engine.user.dto;

/**
 * Response Data Transfer Object (DTO) for a participant.
 *
 * @param id The unique identifier of the participant.
 * @param fullName The full name of the participant.
 * @param phoneNumber The phone number of the participant.
 * @param email The email address of the participant.
 */
public record ParticipantResponse(
        Long id,
        String fullName,
        String phoneNumber,
        String email
) {}