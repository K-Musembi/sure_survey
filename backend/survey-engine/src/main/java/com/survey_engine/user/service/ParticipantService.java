package com.survey_engine.user.service;

import com.survey_engine.user.dto.ParticipantRequest;
import com.survey_engine.user.dto.ParticipantResponse;
import com.survey_engine.user.models.Company;
import com.survey_engine.user.models.Participant;
import com.survey_engine.user.models.User;
import com.survey_engine.user.repository.CompanyRepository;
import com.survey_engine.user.repository.ParticipantRepository;
import com.survey_engine.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Transactional
    public ParticipantResponse createParticipant(ParticipantRequest request, String authUserId) {
        User user = findUserById(authUserId);
        Company company = findCompanyById(request.companyId());

        // Authorization: Ensure the authenticated user belongs to the company they're adding a participant to.
        if (!user.getCompany().getId().equals(company.getId())) {
            throw new AccessDeniedException("You are not authorized to add participants to this company.");
        }

        Participant participant = new Participant();
        participant.setFirstName(request.firstName());
        participant.setLastName(request.lastName());
        participant.setEmail(request.email());
        participant.setPhoneNumber(request.phoneNumber());
        participant.setCompany(company);

        Participant savedParticipant = participantRepository.save(participant);
        return mapToResponse(savedParticipant);
    }

    @Transactional(readOnly = true)
    public ParticipantResponse findParticipantById(Long participantId, String authUserId) {
        User user = findUserById(authUserId);
        Participant participant = findParticipantById(participantId);

        // Authorization: Ensure the user can only view participants of their own company.
        if (!participant.getCompany().getId().equals(user.getCompany().getId())) {
            throw new AccessDeniedException("You are not authorized to view this participant.");
        }

        return mapToResponse(participant);
    }

    @Transactional(readOnly = true)
    public List<ParticipantResponse> findParticipantsByCompany(Long companyId, String authUserId) {
        User user = findUserById(authUserId);

        // Authorization: Ensure the user can only list participants of their own company.
        if (!user.getCompany().getId().equals(companyId)) {
            throw new AccessDeniedException("You are not authorized to list participants for this company.");
        }

        return participantRepository.findByCompanyId(companyId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipantResponse updateParticipant(Long participantId, ParticipantRequest request, String authUserId) {
        User user = findUserById(authUserId);
        Participant participant = findParticipantById(participantId);

        // Authorization: Ensure the user can only update participants of their own company.
        if (!participant.getCompany().getId().equals(user.getCompany().getId())) {
            throw new AccessDeniedException("You are not authorized to update this participant.");
        }

        // Ensure the company isn't being changed to one the user doesn't belong to.
        if (!Objects.equals(request.companyId(), user.getCompany().getId())) {
            throw new AccessDeniedException("You cannot move a participant to a company you do not belong to.");
        }

        participant.setFirstName(request.firstName());
        participant.setLastName(request.lastName());
        participant.setEmail(request.email());
        participant.setPhoneNumber(request.phoneNumber());

        Participant updatedParticipant = participantRepository.save(participant);
        return mapToResponse(updatedParticipant);
    }

    @Transactional
    public void deleteParticipant(Long participantId, String authUserId) {
        User user = findUserById(authUserId);
        Participant participant = findParticipantById(participantId);

        // Authorization: Ensure the user can only delete participants of their own company.
        if (!participant.getCompany().getId().equals(user.getCompany().getId())) {
            throw new AccessDeniedException("You are not authorized to delete this participant.");
        }

        participantRepository.delete(participant);
    }

    private User findUserById(String authUserId) {
        return userRepository.findById(Long.parseLong(authUserId))
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found."));
    }

    private Participant findParticipantById(Long participantId) {
        return participantRepository.findById(participantId)
                .orElseThrow(() -> new EntityNotFoundException("Participant not found with id: " + participantId));
    }

    private Company findCompanyById(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + companyId));
    }

    private ParticipantResponse mapToResponse(Participant participant) {
        return new ParticipantResponse(
                participant.getId(),
                participant.getFirstName(),
                participant.getLastName(),
                participant.getPhoneNumber(),
                participant.getEmail(),
                participant.getCompany().getId()
        );
    }
}