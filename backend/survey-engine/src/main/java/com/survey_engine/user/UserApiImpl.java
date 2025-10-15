package com.survey_engine.user;


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
}
