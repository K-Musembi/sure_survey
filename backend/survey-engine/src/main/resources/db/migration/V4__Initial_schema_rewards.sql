-- Create rewards table
CREATE TABLE rewards (
    id UUID PRIMARY KEY,
    survey_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    reward_type VARCHAR(255) NOT NULL,
    total_amount DECIMAL(10, 2),
    amount_per_recipient DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3),
    provider VARCHAR(255),
    max_recipients INTEGER,
    remaining_rewards INTEGER,
    status VARCHAR(255) NOT NULL,
    tenant_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create loyalty_accounts table
CREATE TABLE loyalty_accounts (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL UNIQUE,
    balance DECIMAL(10, 2) NOT NULL,
    tenant_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create reward_transactions table
CREATE TABLE reward_transactions (
    id UUID PRIMARY KEY,
    reward_id UUID NOT NULL REFERENCES rewards(id) ON DELETE CASCADE,
    participant_id VARCHAR(255) NOT NULL,
    recipient_identifier VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    provider_transaction_id VARCHAR(255),
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE (reward_id, participant_id)
);

-- Create loyalty_transactions table
CREATE TABLE loyalty_transactions (
    id UUID PRIMARY KEY,
    loyalty_account_id UUID NOT NULL REFERENCES loyalty_accounts(id) ON DELETE CASCADE,
    reward_transaction_id UUID REFERENCES reward_transactions(id) ON DELETE SET NULL,
    type VARCHAR(255) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL
);

-- Add indexes for foreign keys
CREATE INDEX idx_rewards_survey_id ON rewards(survey_id);
CREATE INDEX idx_rewards_user_id ON rewards(user_id);
CREATE INDEX idx_loyalty_accounts_user_id ON loyalty_accounts(user_id);
CREATE INDEX idx_reward_transactions_reward_id ON reward_transactions(reward_id);
CREATE INDEX idx_loyalty_transactions_loyalty_account_id ON loyalty_transactions(loyalty_account_id);
