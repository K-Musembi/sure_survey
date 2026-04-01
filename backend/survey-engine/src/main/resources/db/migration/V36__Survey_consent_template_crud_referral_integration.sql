-- V36: Survey consent (ODPC), template CRUD support, referral-business integration link

-- 1. Survey consent log (ODPC compliance for all survey channels)
CREATE TABLE IF NOT EXISTS survey_consent_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    survey_id       BIGINT NOT NULL REFERENCES surveys(id),
    phone_hash      VARCHAR(64),
    participant_id  VARCHAR(255),
    event_type      VARCHAR(20) NOT NULL,
    channel         VARCHAR(20) NOT NULL,
    consent_message TEXT,
    privacy_notice_url VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_survey_consent_log_survey ON survey_consent_log(survey_id);
CREATE INDEX IF NOT EXISTS idx_survey_consent_log_phone ON survey_consent_log(phone_hash);

-- 2. Add consent fields to surveys table
ALTER TABLE surveys ADD COLUMN IF NOT EXISTS requires_consent BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE surveys ADD COLUMN IF NOT EXISTS consent_message TEXT;

-- 3. Add business integration link to referral campaigns
ALTER TABLE referral_campaigns ADD COLUMN IF NOT EXISTS business_integration_id UUID;

-- 4. Seed WHATSAPP_COST_PER_MESSAGE system setting
INSERT INTO system_settings (setting_key, setting_value, description) VALUES
    ('WHATSAPP_COST_PER_MESSAGE', '3.00', 'Cost per WhatsApp message in KES')
ON CONFLICT (setting_key) DO NOTHING;
