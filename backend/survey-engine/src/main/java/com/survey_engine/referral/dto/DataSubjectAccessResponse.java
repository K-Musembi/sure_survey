package com.survey_engine.referral.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response for a Subject Access Request — lists all referral and consent data
 * held for the requesting phone number.
 */
public record DataSubjectAccessResponse(
        String phoneHash,
        LocalDateTime dataRetrievedAt,
        UUID dsrId,
        List<InviteSummary> invites,
        List<ConsentSummary> consentHistory
) {
    public record InviteSummary(
            UUID inviteId,
            String status,
            String channel,
            LocalDateTime inviteSentAt,
            LocalDateTime optedInAt,
            LocalDateTime actionCompletedAt
    ) {}

    public record ConsentSummary(
            UUID logId,
            String eventType,
            String channel,
            boolean referrerConfirmedConsent,
            LocalDateTime recordedAt
    ) {}
}
