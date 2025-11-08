-- Add user_id to billing_subscriptions table
ALTER TABLE billing_subscriptions ADD COLUMN user_id BIGINT;

-- Add user_id to billing_invoices table
ALTER TABLE billing_invoices ADD COLUMN user_id BIGINT;

-- Add foreign key constraint to billing_subscriptions
ALTER TABLE billing_subscriptions
ADD CONSTRAINT fk_billing_subscriptions_user
FOREIGN KEY (user_id)
REFERENCES app_user(id);

-- Add foreign key constraint to billing_invoices
ALTER TABLE billing_invoices
ADD CONSTRAINT fk_billing_invoices_user
FOREIGN KEY (user_id)
REFERENCES app_user(id);
