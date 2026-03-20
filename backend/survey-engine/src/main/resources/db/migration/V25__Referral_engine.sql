-- V25: Referral engine module

-- Closed user groups (for SURVEY_CLOSED campaigns — only eligible phones can be referred)
CREATE TABLE referral_closed_groups (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   BIGINT NOT NULL,
    name        VARCHAR(200) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_referral_closed_groups_tenant ON referral_closed_groups(tenant_id);

CREATE TABLE referral_closed_group_members (
    group_id            UUID NOT NULL REFERENCES referral_closed_groups(id) ON DELETE CASCADE,
    phone               VARCHAR(20) NOT NULL,
    member_identifier   VARCHAR(100),  -- e.g. member number, policy number
    PRIMARY KEY (group_id, phone)
);

-- Campaigns configure referral behaviour per tenant
CREATE TABLE referral_campaigns (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               BIGINT NOT NULL,
    name                    VARCHAR(200) NOT NULL,
    campaign_type           VARCHAR(30) NOT NULL,
    -- SURVEY_CLOSED | SURVEY_OPEN | SERVICE
    survey_id               BIGINT REFERENCES surveys(id) ON DELETE SET NULL,
    closed_group_id         UUID REFERENCES referral_closed_groups(id) ON DELETE SET NULL,
    reward_trigger          VARCHAR(30) NOT NULL,
    -- SURVEY_COMPLETE | SERVICE_ACTIVATION
    referrer_reward_type    VARCHAR(30),             -- AIRTIME | DATA | POINTS
    referrer_reward_value   DECIMAL(10,2),
    max_referrals_per_user  INT NOT NULL DEFAULT 3,
    daily_referral_limit    INT NOT NULL DEFAULT 5,
    invite_expiry_hours     INT NOT NULL DEFAULT 72,
    status                  VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    -- ACTIVE | PAUSED | ENDED
    start_date              DATE,
    end_date                DATE,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_referral_campaigns_tenant ON referral_campaigns(tenant_id);
CREATE INDEX idx_referral_campaigns_survey ON referral_campaigns(survey_id);

-- Unique referral codes — one per referrer per campaign
CREATE TABLE referral_codes (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id             UUID NOT NULL REFERENCES referral_campaigns(id) ON DELETE CASCADE,
    referrer_user_id        BIGINT,            -- null for participant-based referrers
    referrer_participant_id VARCHAR(100),
    code                    VARCHAR(20) NOT NULL UNIQUE,
    total_invites           INT NOT NULL DEFAULT 0,
    successful_referrals    INT NOT NULL DEFAULT 0,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_referral_codes_campaign ON referral_codes(campaign_id);
CREATE INDEX idx_referral_codes_user ON referral_codes(referrer_user_id);

-- Individual referral invites
CREATE TABLE referral_invites (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    referral_code_id    UUID NOT NULL REFERENCES referral_codes(id) ON DELETE CASCADE,
    referred_phone      VARCHAR(20) NOT NULL,
    channel             VARCHAR(20) NOT NULL DEFAULT 'SMS',
    status              VARCHAR(30) NOT NULL DEFAULT 'INVITE_SENT',
    -- INVITE_SENT | OPT_IN_REQUESTED | OPTED_IN | ACTION_COMPLETED | REWARD_TRIGGERED | REWARDED
    -- Terminal: OPTED_OUT | INVALID | EXPIRED
    referrer_notified   BOOLEAN NOT NULL DEFAULT FALSE,
    invite_sent_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    opted_in_at         TIMESTAMP,
    action_completed_at TIMESTAMP,
    rewarded_at         TIMESTAMP,
    expires_at          TIMESTAMP NOT NULL
);

CREATE INDEX idx_referral_invites_phone ON referral_invites(referred_phone);
CREATE INDEX idx_referral_invites_code ON referral_invites(referral_code_id);
CREATE INDEX idx_referral_invites_status ON referral_invites(status);

-- ODPC-compliant consent audit trail — immutable, append-only
CREATE TABLE referral_consent_log (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    referral_invite_id          UUID NOT NULL REFERENCES referral_invites(id),
    phone                       VARCHAR(20) NOT NULL,
    event_type                  VARCHAR(20) NOT NULL,  -- OPT_IN | OPT_OUT
    channel                     VARCHAR(20),
    referrer_confirmed_consent  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_referral_consent_log_invite ON referral_consent_log(referral_invite_id);
