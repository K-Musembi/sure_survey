-- V26: Decision Intelligence Platform module

CREATE TABLE insight_reports (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           BIGINT NOT NULL,
    survey_id           BIGINT REFERENCES surveys(id) ON DELETE SET NULL,
    report_type         VARCHAR(30) NOT NULL,
    -- SINGLE_SURVEY | COMPARATIVE | TREND | EXECUTIVE
    title               VARCHAR(200),
    period_start        DATE,
    period_end          DATE,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    -- PENDING | GENERATING | READY | FAILED
    executive_summary   TEXT,
    key_findings        TEXT,   -- JSON array of {type, text, area}
    recommendations     TEXT,   -- JSON array of {priority, area, recommendedAction, suggestedOwner, suggestedTimeline}
    respondent_clusters TEXT,   -- JSON array of {name, size, description}
    response_count      INT,
    error_message       TEXT,   -- populated on FAILED status
    generated_at        TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_insight_reports_tenant ON insight_reports(tenant_id);
CREATE INDEX idx_insight_reports_survey ON insight_reports(survey_id);
CREATE INDEX idx_insight_reports_status ON insight_reports(tenant_id, status);

CREATE TABLE action_plans (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id           UUID NOT NULL REFERENCES insight_reports(id) ON DELETE CASCADE,
    tenant_id           BIGINT NOT NULL,
    priority            VARCHAR(10) NOT NULL,   -- HIGH | MEDIUM | LOW
    area                VARCHAR(100),
    recommended_action  TEXT NOT NULL,
    suggested_owner     VARCHAR(100),
    suggested_timeline  VARCHAR(50),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    -- PENDING | IN_PROGRESS | COMPLETED | DISMISSED
    completion_notes    TEXT,
    due_date            DATE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_action_plans_report ON action_plans(report_id);
CREATE INDEX idx_action_plans_tenant_status ON action_plans(tenant_id, status);
