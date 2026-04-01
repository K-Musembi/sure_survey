-- V37: Email verification + webhook outbound indexes

-- Add email verification flag to users
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT FALSE;

-- Add index for webhook subscription lookups by tenant + active
CREATE INDEX IF NOT EXISTS idx_webhook_sub_tenant_active
    ON webhook_subscriptions(tenant_id, is_active);
