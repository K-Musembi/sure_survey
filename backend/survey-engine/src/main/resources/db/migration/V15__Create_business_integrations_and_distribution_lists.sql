-- Create distribution_list table
CREATE TABLE distribution_list (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    tenant_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create distribution_list_contacts table
CREATE TABLE distribution_list_contacts (
    id UUID PRIMARY KEY,
    distribution_list_id UUID NOT NULL REFERENCES distribution_list(id) ON DELETE CASCADE,
    phone_number VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255)
);

-- Add distribution_list_id to surveys table
ALTER TABLE surveys
ADD COLUMN distribution_list_id UUID;

-- Add foreign key constraint to surveys table
ALTER TABLE surveys
ADD CONSTRAINT fk_surveys_distribution_list
FOREIGN KEY (distribution_list_id)
REFERENCES distribution_list(id);

-- Create business_integrations table
CREATE TABLE business_integrations (
    id UUID PRIMARY KEY,
    business_name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    survey_id BIGINT NOT NULL,
    shortcode VARCHAR(255),
    consumer_key TEXT,
    consumer_secret TEXT,
    callback_secret_token VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    tenant_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create business_transactions table
CREATE TABLE business_transactions (
    id UUID PRIMARY KEY,
    integration_id UUID NOT NULL REFERENCES business_integrations(id),
    external_transaction_id VARCHAR(255),
    msisdn VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    amount DECIMAL(19, 2),
    transaction_time TIMESTAMP,
    tenant_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Indexes
CREATE INDEX idx_distribution_list_tenant_id ON distribution_list(tenant_id);
CREATE INDEX idx_distribution_list_contacts_list_id ON distribution_list_contacts(distribution_list_id);
CREATE INDEX idx_business_integrations_tenant_id ON business_integrations(tenant_id);
CREATE INDEX idx_business_transactions_integration_id ON business_transactions(integration_id);
