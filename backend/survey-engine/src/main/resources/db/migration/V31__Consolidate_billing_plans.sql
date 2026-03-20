-- V31: Remove legacy Free and any duplicate/stale plans.
-- Ensure Basic, Pro, Enterprise have the correct features (idempotent upsert).
-- Subscriptions pointing to removed plans are cancelled before deletion.

-- 1. Upsert the three canonical plans so features/price are always correct
--    regardless of what V29's ON CONFLICT DO NOTHING may have skipped.
INSERT INTO billing_plans (name, price, billing_interval, features, created_at, updated_at)
VALUES
    (
        'Basic',
        2999.00,
        'MONTHLY',
        '{"maxSurveys":10,"maxResponsesPerSurvey":500,"channels":["WEB","SMS"],"aiAnalysis":false,"referralEngine":false,"performanceSurvey":false,"webhooks":false,"isCustomPricing":false,"displayFeatures":["Up to 10 surveys","500 responses per survey","Web & SMS channels","Basic analytics dashboard","Email support"]}',
        NOW(), NOW()
    ),
    (
        'Pro',
        7999.00,
        'MONTHLY',
        '{"maxSurveys":50,"maxResponsesPerSurvey":5000,"channels":["WEB","SMS","USSD","MOBILE"],"aiAnalysis":true,"referralEngine":true,"performanceSurvey":true,"webhooks":false,"isCustomPricing":false,"displayFeatures":["Up to 50 surveys","5,000 responses per survey","All channels (Web, SMS, USSD, Mobile)","AI-powered insights & reports","Referral engine","Performance evaluations","Priority email & chat support"]}',
        NOW(), NOW()
    ),
    (
        'Enterprise',
        0.00,
        'MONTHLY',
        '{"maxSurveys":-1,"maxResponsesPerSurvey":-1,"channels":["WEB","SMS","USSD","MOBILE","API"],"aiAnalysis":true,"referralEngine":true,"performanceSurvey":true,"webhooks":true,"isCustomPricing":true,"displayFeatures":["Unlimited surveys","Unlimited responses","All channels + API access","Advanced AI insights & custom reports","Full referral & decision intelligence platform","Outbound webhooks & integrations","Dedicated account manager & SLA"]}',
        NOW(), NOW()
    )
ON CONFLICT (name) DO UPDATE
    SET price            = EXCLUDED.price,
        billing_interval = EXCLUDED.billing_interval,
        features         = EXCLUDED.features,
        updated_at       = NOW();

-- 2. Cancel any active subscriptions pointing to plans that will be removed,
--    then migrate them to the Basic plan so no orphaned subscriptions remain.
UPDATE billing_subscriptions
SET plan_id    = (SELECT id FROM billing_plans WHERE name = 'Basic'),
    status     = 'CANCELLED',
    updated_at = NOW()
WHERE plan_id IN (
    SELECT id FROM billing_plans
    WHERE name NOT IN ('Basic', 'Pro', 'Enterprise')
);

-- 3. Delete gateway mappings for plans being removed (child rows first)
DELETE FROM billing_plan_gateways
WHERE plan_id IN (
    SELECT id FROM billing_plans
    WHERE name NOT IN ('Basic', 'Pro', 'Enterprise')
);

-- 4. Remove all plans that are not the three canonical tiers
DELETE FROM billing_plans
WHERE name NOT IN ('Basic', 'Pro', 'Enterprise');
