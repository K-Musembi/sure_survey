-- V27: Survey template marketplace + outbound webhook notifications

-- Industry-tagged survey templates sharable across tenants
CREATE TABLE survey_templates (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    industry        VARCHAR(100),   -- SACCO | INSURANCE | NGO | RETAIL | HR | GENERAL
    survey_type     VARCHAR(30) NOT NULL,  -- maps to SurveyType enum
    is_public       BOOLEAN NOT NULL DEFAULT FALSE,
    created_by      BIGINT,             -- tenant_id of creator; null = system template
    usage_count     INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_survey_templates_industry ON survey_templates(industry);
CREATE INDEX idx_survey_templates_public ON survey_templates(is_public);

CREATE TABLE survey_template_questions (
    id              BIGSERIAL PRIMARY KEY,
    template_id     BIGINT NOT NULL REFERENCES survey_templates(id) ON DELETE CASCADE,
    question_text   TEXT NOT NULL,
    question_type   VARCHAR(50) NOT NULL,
    options         TEXT,
    position        INT NOT NULL,
    weight          DECIMAL(5,2) DEFAULT 1.0,
    score_map       TEXT,
    category        VARCHAR(100)
);

CREATE INDEX idx_stq_template ON survey_template_questions(template_id);

-- Outbound webhook subscriptions (tenants receive events on their systems)
CREATE TABLE webhook_subscriptions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       BIGINT NOT NULL,
    target_url      VARCHAR(500) NOT NULL,
    event_types     TEXT NOT NULL,  -- JSON array: ["SURVEY_COMPLETED", "REPORT_READY", "REFERRAL_CONVERTED"]
    secret          VARCHAR(100),   -- HMAC signing secret
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_webhook_subscriptions_tenant ON webhook_subscriptions(tenant_id);

-- Delivery log for outbound webhooks
CREATE TABLE webhook_delivery_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id UUID NOT NULL REFERENCES webhook_subscriptions(id),
    event_type      VARCHAR(50) NOT NULL,
    payload         TEXT NOT NULL,
    http_status     INT,
    response_body   TEXT,
    delivered_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    success         BOOLEAN NOT NULL DEFAULT FALSE
);
