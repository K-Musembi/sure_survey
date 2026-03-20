package com.survey_engine.user.dto;

public record AuthenticationResult(
        String token,
        UserResponse user
) {}
