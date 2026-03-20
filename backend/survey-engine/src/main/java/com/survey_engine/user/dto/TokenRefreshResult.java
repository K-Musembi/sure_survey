package com.survey_engine.user.dto;

public record TokenRefreshResult(
        String accessToken,
        String refreshToken
) {}
