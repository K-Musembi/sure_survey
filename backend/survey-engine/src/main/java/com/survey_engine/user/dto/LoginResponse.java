package com.survey_engine.user.dto;

import com.survey_engine.user.models.User;

public record LoginResponse(
        String token,
        User user) {
}
