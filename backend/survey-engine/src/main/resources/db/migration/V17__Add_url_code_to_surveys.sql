-- Add url_code column to surveys table
ALTER TABLE surveys ADD COLUMN url_code VARCHAR(255);

-- Backfill existing surveys with a random UUID
-- Using gen_random_uuid() if available (PostgreSQL 13+)
UPDATE surveys SET url_code = gen_random_uuid()::text WHERE url_code IS NULL;

-- Enforce NOT NULL constraint
ALTER TABLE surveys ALTER COLUMN url_code SET NOT NULL;

-- Ensure uniqueness
ALTER TABLE surveys ADD CONSTRAINT uc_surveys_url_code UNIQUE (url_code);
