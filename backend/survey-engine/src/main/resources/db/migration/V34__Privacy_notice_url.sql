-- V34: ODPC Privacy Notice — store the public privacy notice URL that is appended
-- to all outbound referral SMS messages as required by the Kenya Data Protection Act 2019.

INSERT INTO system_settings (setting_key, setting_value, description)
VALUES (
    'PRIVACY_NOTICE_URL',
    'https://suresurvey.co/privacy',
    'URL appended to referral SMS messages as required by ODPC Data Protection Act 2019'
)
ON CONFLICT (setting_key) DO NOTHING;
