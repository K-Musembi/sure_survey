package com.survey_engine.user;

import org.springframework.modulith.NamedInterface;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@NamedInterface
public interface UserApi {
    Optional<String> findParticipantIdByPhoneNumber(String phoneNumber);

    Optional<String> findPhoneNumberByParticipantId(String participantId);

    Optional<UserDetails> findUserDetailsByEmail(String email);
}
