package com.survey_engine.common.events;

public record UserRegisteredEvent(
        Long userId,
        Long tenantId
) {}