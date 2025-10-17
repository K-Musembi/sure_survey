package com.survey_engine.user.service;

import com.survey_engine.user.dto.ParticipantRequest;
import com.survey_engine.user.dto.ParticipantResponse;
import com.survey_engine.user.models.Participant;
import com.survey_engine.user.repository.ParticipantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    @Transactional
    public ParticipantResponse createParticipant(ParticipantRequest request) {
        Participant participant = new Participant();
        participant.setFullName(request.fullName());
        participant.setEmail(request.email());
        participant.setPhoneNumber(request.phoneNumber());

        Participant savedParticipant = participantRepository.save(participant);
        return mapToResponse(savedParticipant);
    }

    @Transactional(readOnly = true)
    public ParticipantResponse findParticipantById(Long participantId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new EntityNotFoundException("Participant not found with id: " + participantId));

        return mapToResponse(participant);
    }

    @Transactional
    public void deleteParticipant(Long participantId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new EntityNotFoundException("Participant not found with id: " + participantId));

        participantRepository.delete(participant);
    }

    private ParticipantResponse mapToResponse(Participant participant) {
        return new ParticipantResponse(
                participant.getId(),
                participant.getFullName(),
                participant.getPhoneNumber(),
                participant.getEmail()
        );
    }
}