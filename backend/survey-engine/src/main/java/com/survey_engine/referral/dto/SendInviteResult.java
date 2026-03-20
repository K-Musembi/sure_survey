package com.survey_engine.referral.dto;

import java.util.UUID;

public record SendInviteResult(
        boolean success,
        UUID inviteId,
        String referralCode,
        String message
) {
    public static SendInviteResult success(UUID inviteId, String referralCode) {
        return new SendInviteResult(true, inviteId, referralCode, "Invite sent successfully.");
    }

    public static SendInviteResult invalid(String reason) {

        return new SendInviteResult(false, null, null, reason);
    }
}
