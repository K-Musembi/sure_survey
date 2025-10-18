ALTER TABLE surveys ADD COLUMN tenant_id BIGINT;
ALTER TABLE rewards ADD COLUMN tenant_id BIGINT;
ALTER TABLE loyalty_accounts ADD COLUMN tenant_id BIGINT;
ALTER TABLE payment_event ADD COLUMN tenant_id BIGINT;
ALTER TABLE transactions ADD COLUMN tenant_id BIGINT;

ALTER TABLE surveys ADD CONSTRAINT fk_surveys_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id);
ALTER TABLE rewards ADD CONSTRAINT fk_rewards_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id);
ALTER TABLE loyalty_accounts ADD CONSTRAINT fk_loyalty_accounts_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id);
ALTER TABLE payment_event ADD CONSTRAINT fk_payment_event_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id);
ALTER TABLE transactions ADD CONSTRAINT fk_transactions_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id);
