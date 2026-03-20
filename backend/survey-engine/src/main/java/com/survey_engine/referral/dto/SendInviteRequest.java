package com.survey_engine.referral.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SendInviteRequest(
        @NotNull
        UUID campaignId,
        @NotNull
        Long referrerUserId,
        @NotBlank
        String referredPhone,
        String channel,                     // SMS | WHATSAPP (default SMS)
        boolean referrerConfirmedConsent    // ODPC: referrer declared friend agreed
) {}
