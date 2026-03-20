-- V29: Seed three billing tiers (Basic, Pro, Enterprise)
-- Enterprise uses custom pricing (price = 0, isCustomPricing = true in features JSON)

INSERT INTO billing_plans (name, price, billing_interval, features, created_at, updated_at)
VALUES
    (
        'Basic',
        2999.00,
        'MONTHLY',
        '{
          "maxSurveys": 10,
          "maxResponsesPerSurvey": 500,
          "channels": ["WEB", "SMS"],
          "aiAnalysis": false,
          "referralEngine": false,
          "performanceSurvey": false,
          "webhooks": false,
          "isCustomPricing": false,
          "displayFeatures": [
            "Up to 10 surveys",
            "500 responses per survey",
            "Web & SMS channels",
            "Basic analytics dashboard",
            "Email support"
          ]
        }',
        NOW(),
        NOW()
    ),
    (
        'Pro',
        7999.00,
        'MONTHLY',
        '{
          "maxSurveys": 50,
          "maxResponsesPerSurvey": 5000,
          "channels": ["WEB", "SMS", "USSD", "MOBILE"],
          "aiAnalysis": true,
          "referralEngine": true,
          "performanceSurvey": true,
          "webhooks": false,
          "isCustomPricing": false,
          "displayFeatures": [
            "Up to 50 surveys",
            "5,000 responses per survey",
            "All channels (Web, SMS, USSD, Mobile)",
            "AI-powered insights & reports",
            "Referral engine",
            "Performance evaluations",
            "Priority email & chat support"
          ]
        }',
        NOW(),
        NOW()
    ),
    (
        'Enterprise',
        0.00,
        'MONTHLY',
        '{
          "maxSurveys": -1,
          "maxResponsesPerSurvey": -1,
          "channels": ["WEB", "SMS", "USSD", "MOBILE", "API"],
          "aiAnalysis": true,
          "referralEngine": true,
          "performanceSurvey": true,
          "webhooks": true,
          "isCustomPricing": true,
          "displayFeatures": [
            "Unlimited surveys",
            "Unlimited responses",
            "All channels + API access",
            "Advanced AI insights & custom reports",
            "Full referral & decision intelligence platform",
            "Outbound webhooks & integrations",
            "Dedicated account manager & SLA"
          ]
        }',
        NOW(),
        NOW()
    )
ON CONFLICT (name) DO NOTHING;
