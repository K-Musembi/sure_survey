-- Add the reward_provider column, allowing nulls initially to handle existing data
ALTER TABLE rewards ADD COLUMN reward_provider VARCHAR(255);

-- Set a default value for existing rows where the new column is null.
-- 'AFRICAS_TALKING' is chosen as a sensible default.
UPDATE rewards SET reward_provider = 'AFRICAS_TALKING' WHERE reward_provider IS NULL;

-- Now that all rows have a value, enforce the NOT NULL constraint
ALTER TABLE rewards ALTER COLUMN reward_provider SET NOT NULL;
