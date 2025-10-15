package com.survey_engine.user;

import org.springframework.modulith.NamedInterface;

import java.util.Optional;

@NamedInterface
public interface UserApi {
    Optional<String> findParticipantIdByPhoneNumber(String phoneNumber);
}
