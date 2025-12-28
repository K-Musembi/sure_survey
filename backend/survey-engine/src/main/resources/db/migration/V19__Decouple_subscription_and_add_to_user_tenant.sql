-- Add subscription_id to app_user
ALTER TABLE app_user ADD COLUMN subscription_id UUID;

-- Add subscription_id to tenants
ALTER TABLE tenants ADD COLUMN subscription_id UUID;

-- Generalize billing_subscriptions columns
ALTER TABLE billing_subscriptions RENAME COLUMN paystack_subscription_id TO gateway_subscription_id;
ALTER TABLE billing_subscriptions RENAME COLUMN paystack_email_token TO gateway_email_token;
ALTER TABLE billing_subscriptions ADD COLUMN gateway_type VARCHAR(50);

-- Update existing subscriptions to have PAYSTACK as gateway if they have a gateway_subscription_id
UPDATE billing_subscriptions SET gateway_type = 'PAYSTACK' WHERE gateway_subscription_id IS NOT NULL;
