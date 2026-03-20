-- V30: Add SMS cost per message to system settings
INSERT INTO system_settings (setting_key, setting_value, description)
VALUES ('SMS_COST_PER_MESSAGE', '2.00', 'Cost in KES charged per SMS sent via survey distribution')
ON CONFLICT (setting_key) DO NOTHING;
