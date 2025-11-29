-- Create system_settings table
CREATE TABLE system_settings (
    setting_key VARCHAR(255) PRIMARY KEY,
    setting_value VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create tenant_wallets table
CREATE TABLE tenant_wallets (
    id UUID PRIMARY KEY,
    tenant_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    currency VARCHAR(3) NOT NULL DEFAULT 'KES',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create wallet_transactions table
CREATE TABLE wallet_transactions (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL REFERENCES tenant_wallets(id),
    amount DECIMAL(19, 4) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL, -- CREDIT, DEBIT
    reference_id VARCHAR(255), -- e.g., Payment ID or Survey ID
    description TEXT,
    created_at TIMESTAMP NOT NULL
);

-- Add columns to surveys table
ALTER TABLE surveys
ADD COLUMN target_respondents INTEGER,
ADD COLUMN budget DECIMAL(19, 4);

-- Indexes
CREATE INDEX idx_wallet_transactions_wallet_id ON wallet_transactions(wallet_id);
