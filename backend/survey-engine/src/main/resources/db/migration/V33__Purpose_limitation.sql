-- V33: ODPC Purpose Limitation — add purpose description and consent versioning
-- to referral campaigns and consent logs.

ALTER TABLE referral_campaigns
    ADD COLUMN purpose_description TEXT,
    ADD COLUMN consent_version      INTEGER NOT NULL DEFAULT 1;

ALTER TABLE referral_consent_log
    ADD COLUMN purpose_snapshot TEXT,       -- snapshot of purpose at time of consent (immutable)
    ADD COLUMN consent_version  INTEGER NOT NULL DEFAULT 1;  -- version snapshotted at consent time
