-- Create table for mapping plans to payment gateways
CREATE TABLE billing_plan_gateways (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL REFERENCES billing_plans(id) ON DELETE CASCADE,
    gateway_type VARCHAR(50) NOT NULL, -- e.g. 'PAYSTACK'
    gateway_plan_code VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(plan_id, gateway_type)
);

-- Migrate existing Paystack plan codes to the new table
INSERT INTO billing_plan_gateways (plan_id, gateway_type, gateway_plan_code, created_at, updated_at)
SELECT id, 'PAYSTACK', paystack_plan_code, created_at, updated_at
FROM billing_plans
WHERE paystack_plan_code IS NOT NULL;

-- Remove the tightly coupled column from the plans table
ALTER TABLE billing_plans DROP COLUMN paystack_plan_code;
