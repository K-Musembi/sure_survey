-- Rename tenant_wallets to wallets
ALTER TABLE tenant_wallets RENAME TO wallets;

-- Add user_id column to wallets
ALTER TABLE wallets ADD COLUMN user_id BIGINT;

-- Drop the old unique constraint on tenant_id (which assumed one wallet per tenant)
-- Note: PostgreSQL default naming for UNIQUE(tenant_id) is tenant_wallets_tenant_id_key
ALTER TABLE wallets DROP CONSTRAINT IF EXISTS tenant_wallets_tenant_id_key;

-- Add unique constraint for individual user wallets
-- (One user can have only one wallet)
ALTER TABLE wallets ADD CONSTRAINT wallets_user_id_key UNIQUE (user_id);

-- Add unique constraint for enterprise wallets
-- (One tenant can have only one shared enterprise wallet where user_id is null)
CREATE UNIQUE INDEX idx_unique_enterprise_wallet ON wallets (tenant_id) WHERE user_id IS NULL;

-- Add foreign key constraint to user
ALTER TABLE wallets
ADD CONSTRAINT fk_wallets_user
FOREIGN KEY (user_id)
REFERENCES app_user(id);

-- The foreign key in wallet_transactions will automatically point to the renamed 'wallets' table.
