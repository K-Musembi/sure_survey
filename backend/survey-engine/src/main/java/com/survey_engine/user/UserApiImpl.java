package com.survey_engine.user;


import com.survey_engine.user.models.Participant;
import com.survey_engine.user.models.User;
import com.survey_engine.user.repository.ParticipantRepository;
import com.survey_engine.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserApiImpl implements UserApi {

    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;

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

    @Override
    public Optional<UserDetails> findUserDetailsByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> user);
    }
}
