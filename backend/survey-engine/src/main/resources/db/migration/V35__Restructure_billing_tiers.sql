-- V35: Restructure billing tiers.
-- Basic: Free (KES 0), 5 surveys, 25 responses/survey, Web only
-- Pro: KES 5,999/mo, 10 surveys, 500 responses/survey, Web + SMS + WhatsApp, referral, basic AI
-- Enterprise: KES 19,999/mo, unlimited, all features including BI + performance surveys

UPDATE billing_plans
SET price            = 0.00,
    features         = '{"maxSurveys":5,"maxResponsesPerSurvey":25,"channels":["WEB"],"aiAnalysis":false,"referralEngine":false,"performanceSurvey":false,"webhooks":false,"isCustomPricing":false,"businessIntelligence":false,"displayFeatures":["Up to 5 surveys","25 responses per survey","Web channel only","Basic analytics dashboard","Community support"]}',
    updated_at       = NOW()
WHERE name = 'Basic';

UPDATE billing_plans
SET price            = 5999.00,
    features         = '{"maxSurveys":10,"maxResponsesPerSurvey":500,"channels":["WEB","SMS","WHATSAPP"],"aiAnalysis":true,"referralEngine":true,"performanceSurvey":false,"webhooks":false,"isCustomPricing":false,"businessIntelligence":false,"displayFeatures":["Up to 10 surveys","500 responses per survey","Web, SMS & WhatsApp channels","AI-powered analysis","Referral engine","Email & chat support"]}',
    updated_at       = NOW()
WHERE name = 'Pro';

UPDATE billing_plans
SET price            = 19999.00,
    features         = '{"maxSurveys":-1,"maxResponsesPerSurvey":-1,"channels":["WEB","SMS","WHATSAPP","USSD","MOBILE","API"],"aiAnalysis":true,"referralEngine":true,"performanceSurvey":true,"webhooks":true,"isCustomPricing":false,"businessIntelligence":true,"displayFeatures":["Unlimited surveys","Unlimited responses","All channels + API access","Advanced AI insights & custom reports","Full referral & decision intelligence platform","Performance evaluations","Outbound webhooks & integrations","Business intelligence dashboards","Dedicated account manager & SLA"]}',
    updated_at       = NOW()
WHERE name = 'Enterprise';
