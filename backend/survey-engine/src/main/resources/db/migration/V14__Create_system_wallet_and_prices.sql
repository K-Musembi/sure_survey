-- Create system_wallet table
CREATE TABLE system_wallet (
    wallet_type VARCHAR(50) PRIMARY KEY, -- AIRTIME_STOCK, DATA_BUNDLE_STOCK
    current_balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    reserved_balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    updated_at TIMESTAMP
);

-- Insert default system wallets
INSERT INTO system_wallet (wallet_type, current_balance, reserved_balance, updated_at)
VALUES ('AIRTIME_STOCK', 0.0000, 0.0000, CURRENT_TIMESTAMP);

INSERT INTO system_wallet (wallet_type, current_balance, reserved_balance, updated_at)
VALUES ('DATA_BUNDLE_STOCK', 0.0000, 0.0000, CURRENT_TIMESTAMP);

-- Add new system settings for reward retail prices
INSERT INTO system_settings (setting_key, setting_value, description, created_at, updated_at)
VALUES 
    ('AIRTIME_RETAIL_PRICE_20', '20.00', 'Retail price for 20 KES airtime bundle', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('AIRTIME_RETAIL_PRICE_50', '50.00', 'Retail price for 50 KES airtime bundle', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('AIRTIME_RETAIL_PRICE_100', '100.00', 'Retail price for 100 KES airtime bundle', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('DATA_BUNDLE_PRICE_20MB', '15.00', 'Retail price for 20MB data bundle', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('DATA_BUNDLE_PRICE_100MB', '50.00', 'Retail price for 100MB data bundle', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
