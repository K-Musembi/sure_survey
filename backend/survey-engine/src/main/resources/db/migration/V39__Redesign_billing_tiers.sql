-- V39: Redesign billing tiers per new requirements.
-- Free tier is now implicit (no subscription record) — 3 surveys, 25 responses, WEB only.
-- Basic: KES 499/mo, 5 surveys, 100 responses/survey, Web only, traditional analytics
-- Pro: KES 5,999/mo, unlimited surveys/responses, Web + SMS + WhatsApp, rewards, referral, performance, traditional analytics
-- Enterprise: KES 9,999/mo, all Pro features + AI analytics, decision intelligence, webhooks, API

INSERT INTO billing_plans (name, price, billing_interval, features, created_at, updated_at)
VALUES (
    'Basic',
    499.00,
    'MONTHLY',
    '{"maxSurveys":5,"maxResponsesPerSurvey":100,"channels":["WEB"],"aiAnalysis":false,"referralEngine":false,"performanceSurvey":false,"rewards":false,"webhooks":false,"isCustomPricing":false,"businessIntelligence":false,"displayFeatures":["Up to 5 surveys","100 responses per survey","Web channel only","Traditional dashboard analytics","Email support"]}',
    NOW(),
    NOW()
)
ON CONFLICT (name) DO UPDATE SET
    price      = EXCLUDED.price,
    features   = EXCLUDED.features,
    updated_at = NOW();

INSERT INTO billing_plans (name, price, billing_interval, features, created_at, updated_at)
VALUES (
    'Pro',
    5999.00,
    'MONTHLY',
    '{"maxSurveys":-1,"maxResponsesPerSurvey":-1,"channels":["WEB","SMS","WHATSAPP"],"aiAnalysis":false,"referralEngine":true,"performanceSurvey":true,"rewards":true,"webhooks":false,"isCustomPricing":false,"businessIntelligence":false,"displayFeatures":["Unlimited surveys","Unlimited responses","Web, SMS & WhatsApp channels","Referral engine","Competition surveys","Survey rewards (airtime, data, loyalty)","Traditional dashboard analytics","Priority support"]}',
    NOW(),
    NOW()
)
ON CONFLICT (name) DO UPDATE SET
    price      = EXCLUDED.price,
    features   = EXCLUDED.features,
    updated_at = NOW();

INSERT INTO billing_plans (name, price, billing_interval, features, created_at, updated_at)
VALUES (
    'Enterprise',
    9999.00,
    'MONTHLY',
    '{"maxSurveys":-1,"maxResponsesPerSurvey":-1,"channels":["WEB","SMS","WHATSAPP"],"aiAnalysis":true,"referralEngine":true,"performanceSurvey":true,"rewards":true,"webhooks":true,"isCustomPricing":false,"businessIntelligence":true,"displayFeatures":["Everything in Pro","AI-powered survey analysis","Decision intelligence platform","Outbound webhooks & API access","Business intelligence dashboards","Dedicated account manager"]}',
    NOW(),
    NOW()
)
ON CONFLICT (name) DO UPDATE SET
    price      = EXCLUDED.price,
    features   = EXCLUDED.features,
    updated_at = NOW();
