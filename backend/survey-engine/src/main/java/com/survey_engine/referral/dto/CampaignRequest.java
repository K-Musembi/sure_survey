package com.survey_engine.referral.dto;


import com.survey_engine.referral.domain.enums.CampaignType;
import com.survey_engine.referral.domain.enums.RewardTrigger;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CampaignRequest(
        @NotBlank
        String name,
        @NotNull
        CampaignType campaignType,
        Long surveyId,
        UUID closedGroupId,
        @NotNull
        RewardTrigger rewardTrigger,
        String referrerRewardType,
        BigDecimal referrerRewardValue,
        int maxReferralsPerUser,
        int dailyReferralLimit,
        int inviteExpiryHours,
        LocalDate startDate,
        LocalDate endDate,
        /* Describes the specific purpose for which referral data is collected (ODPC Art. 25). */
        String purposeDescription
) {}
