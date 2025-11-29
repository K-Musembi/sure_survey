package com.survey_engine.user.dto;

import com.survey_engine.common.enums.SettingKey;

/**
 * Response DTO for a system setting.
 *
 * @param key The unique key of the setting.
 * @param value The current value of the setting.
 * @param description A description of what this setting controls.
 */
public record SystemSettingResponse(
        SettingKey key,
        String value,
        String description
) {}
