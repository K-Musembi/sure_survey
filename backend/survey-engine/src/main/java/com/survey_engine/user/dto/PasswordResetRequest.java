package com.survey_engine.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for the password reset step (after clicking the reset link).
 *
 * @param token       The one-time reset token from the email link.
 * @param newPassword The user's chosen new password.
 */
public record PasswordResetRequest(
        @NotBlank(message = "Reset token is required")
        String token,

        @NotBlank(message = "New password is required")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,72}$",
                message = "Password must be 8-72 characters with at least one uppercase, one lowercase, one digit, and one special character.")
        String newPassword
) {}
