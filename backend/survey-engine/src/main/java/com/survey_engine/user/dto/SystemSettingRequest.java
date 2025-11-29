package com.survey_engine.user.dto;

import com.survey_engine.common.enums.SettingKey;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating a system setting.
 *
 * @param key The enum key of the setting (e.g., ENTERPRISE_SURVEY_COST_PER_RESPONDENT).
 * @param value The new value for the setting (e.g., "5.00").
 */
public record SystemSettingRequest(
        @NotNull(message = "Setting key is required")
        SettingKey key,

        @NotBlank(message = "Setting value is required")
        String value
) {}
