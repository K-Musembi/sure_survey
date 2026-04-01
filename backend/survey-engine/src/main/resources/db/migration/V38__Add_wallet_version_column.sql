-- V38: Add optimistic locking version column to wallets table
ALTER TABLE wallets ADD COLUMN version BIGINT DEFAULT 0;
