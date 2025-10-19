package com.survey_engine.user.service;

import com.survey_engine.user.dto.ParticipantRequest;
import com.survey_engine.user.dto.ParticipantResponse;
import com.survey_engine.user.models.Participant;
import com.survey_engine.user.repository.ParticipantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing {@link Participant} entities.
 * Provides business logic for creating, retrieving, and deleting participants.
 */
@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    /**
     * Creates a new participant from the provided request data.
     *
     * @param request The {@link ParticipantRequest} containing the details for the new participant.
     * @return A {@link ParticipantResponse} DTO representing the newly created participant.
     */
    @Transactional
    public ParticipantResponse createParticipant(ParticipantRequest request) {
        Participant participant = new Participant();
        participant.setFullName(request.fullName());
        participant.setEmail(request.email());
        participant.setPhoneNumber(request.phoneNumber());

        Participant savedParticipant = participantRepository.save(participant);
        return mapToResponse(savedParticipant);
    }

    /**
     * Finds a participant by their unique ID.
     *
     * @param participantId The ID of the participant to find.
     * @return A {@link ParticipantResponse} DTO representing the found participant.
     * @throws EntityNotFoundException if no participant is found with the given ID.
     */
    @Transactional(readOnly = true)
    public ParticipantResponse findParticipantById(Long participantId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new EntityNotFoundException("Participant not found with id: " + participantId));

        return mapToResponse(participant);
    }

    /**
     * Deletes a participant by their unique ID.
     *
     * @param participantId The ID of the participant to delete.
     * @throws EntityNotFoundException if no participant is found with the given ID.
     */
    @Transactional
    public void deleteParticipant(Long participantId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new EntityNotFoundException("Participant not found with id: " + participantId));

        participantRepository.delete(participant);
    }

    /**
     * Maps a {@link Participant} entity to a {@link ParticipantResponse} DTO.
     *
     * @param participant The {@link Participant} entity to map.
     * @return The corresponding {@link ParticipantResponse} DTO.
     */
    private ParticipantResponse mapToResponse(Participant participant) {
        return new ParticipantResponse(
                participant.getId(),
                participant.getFullName(),
                participant.getPhoneNumber(),
                participant.getEmail()
        );
    }
}
