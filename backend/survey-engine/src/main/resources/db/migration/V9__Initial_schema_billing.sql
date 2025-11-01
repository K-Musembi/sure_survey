-- Create billing_plans table
CREATE TABLE billing_plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    paystack_plan_code VARCHAR(255) NOT NULL UNIQUE,
    price DECIMAL(10, 2) NOT NULL,
    billing_interval VARCHAR(255) NOT NULL,
    features TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create billing_subscriptions table
CREATE TABLE billing_subscriptions (
    id UUID PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL REFERENCES billing_plans(id),
    paystack_subscription_id VARCHAR(255) UNIQUE,
    status VARCHAR(255) NOT NULL,
    current_period_start TIMESTAMP,
    current_period_end TIMESTAMP,
    trial_end_date TIMESTAMP,
    paystack_email_token VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create billing_invoices table
CREATE TABLE billing_invoices (
    id UUID PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    subscription_id UUID REFERENCES billing_subscriptions(id),
    paystack_invoice_id VARCHAR(255) UNIQUE,
    status VARCHAR(255) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    due_date TIMESTAMP,
    paid_at TIMESTAMP,
    invoice_pdf_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Add indexes for foreign keys
CREATE INDEX idx_billing_subscriptions_plan_id ON billing_subscriptions(plan_id);
CREATE INDEX idx_billing_invoices_subscription_id ON billing_invoices(subscription_id);
