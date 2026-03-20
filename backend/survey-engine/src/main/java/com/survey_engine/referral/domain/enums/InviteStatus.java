package com.survey_engine.referral.domain.enums;

public enum InviteStatus {
    // Active flow
    INVITE_SENT,
    OPT_IN_REQUESTED,
    OPTED_IN,
    ACTION_COMPLETED,
    REWARD_TRIGGERED,
    REWARDED,
    // Terminal negative states
    OPTED_OUT,
    INVALID,    // phone not in closed group
    EXPIRED
}
