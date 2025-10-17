package com.survey_engine.user;


import com.survey_engine.user.models.Participant;
import com.survey_engine.user.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserApiImpl implements UserApi {

    private final ParticipantRepository participantRepository;

    @Override
    public Optional<String> findParticipantIdByPhoneNumber(String phoneNumber) {
        return participantRepository.findByPhoneNumber(phoneNumber)
                .map(participant -> String.valueOf(participant.getId()));
    }

    @Override
    public Optional<String> findPhoneNumberByParticipantId(String participantId) {
        try {
            Long id = Long.parseLong(participantId);
            return participantRepository.findById(id).map(Participant::getPhoneNumber);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
