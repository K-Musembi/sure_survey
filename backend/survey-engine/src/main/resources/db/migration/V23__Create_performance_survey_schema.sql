-- Alter existing tables to support new features
ALTER TABLE responses ADD COLUMN metadata TEXT;
ALTER TABLE business_transactions ADD COLUMN subject_reference VARCHAR(255);

-- Performance Survey Module Tables

-- 1. Org Units (Hierarchy)
CREATE TABLE ps_org_units (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- REGION, BRANCH, TEAM, CUSTOM
    parent_id UUID REFERENCES ps_org_units(id),
    manager_id VARCHAR(255), -- Link to app_user(id) or external
    tenant_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_ps_org_units_tenant ON ps_org_units(tenant_id);
CREATE INDEX idx_ps_org_units_parent ON ps_org_units(parent_id);

-- 2. Performance Subjects (Replaces old OrgMember concept)
CREATE TABLE ps_performance_subjects (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255), -- Optional link to app_user
    reference_code VARCHAR(255) NOT NULL, -- External ID (e.g. Agent ID, Till Number)
    display_name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- INDIVIDUAL, BUSINESS_UNIT
    org_unit_id UUID NOT NULL REFERENCES ps_org_units(id),
    role VARCHAR(50) NOT NULL, -- MEMBER, LEAD
    tenant_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX idx_ps_subject_ref ON ps_performance_subjects(reference_code, tenant_id);
CREATE INDEX idx_ps_subject_user ON ps_performance_subjects(user_id);
CREATE INDEX idx_ps_subject_org_unit ON ps_performance_subjects(org_unit_id);

-- 3. Scoring Schema (Configuration)
CREATE TABLE ps_survey_scoring_schemas (
    id UUID PRIMARY KEY,
    survey_id BIGINT NOT NULL UNIQUE,
    default_question_weight DECIMAL(10, 2),
    target_score DECIMAL(10, 2),
    tenant_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 4. Question Scoring Rules
CREATE TABLE ps_question_scoring_rules (
    id UUID PRIMARY KEY,
    schema_id UUID NOT NULL REFERENCES ps_survey_scoring_schemas(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL,
    weight DECIMAL(10, 2) NOT NULL DEFAULT 1.0,
    scoring_strategy VARCHAR(50) NOT NULL, -- DIRECT_VALUE, OPTION_MAP
    option_score_map TEXT, -- JSON
    tenant_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_ps_rules_schema ON ps_question_scoring_rules(schema_id);

-- 5. Performance Records (Results)
CREATE TABLE ps_performance_records (
    id UUID PRIMARY KEY,
    survey_id BIGINT NOT NULL,
    response_id BIGINT NOT NULL UNIQUE,
    subject_id UUID NOT NULL REFERENCES ps_performance_subjects(id),
    evaluator_user_id VARCHAR(255), -- Optional (who gave the feedback)
    raw_score DECIMAL(10, 2) NOT NULL,
    normalized_score DECIMAL(10, 2) NOT NULL,
    org_unit_id UUID, -- Snapshot of unit at time of record
    recorded_at TIMESTAMP NOT NULL,
    tenant_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_ps_records_subject ON ps_performance_records(subject_id);
CREATE INDEX idx_ps_records_survey ON ps_performance_records(survey_id);
CREATE INDEX idx_ps_records_org_unit ON ps_performance_records(org_unit_id);

-- 6. Aggregated Scores (Dashboard Cache)
CREATE TABLE ps_aggregated_scores (
    id UUID PRIMARY KEY,
    org_unit_id UUID NOT NULL REFERENCES ps_org_units(id) ON DELETE CASCADE,
    period VARCHAR(50) NOT NULL, -- WEEKLY, MONTHLY...
    start_date DATE NOT NULL,
    score DECIMAL(10, 2) NOT NULL,
    sample_size INTEGER NOT NULL,
    tenant_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_ps_agg_score_unit_period ON ps_aggregated_scores(org_unit_id, period, start_date);

-- 7. Gamification Profiles
CREATE TABLE ps_gamification_profiles (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL UNIQUE,
    total_points BIGINT DEFAULT 0,
    current_streak INTEGER DEFAULT 0,
    level INTEGER DEFAULT 1,
    tenant_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 8. Badge Definitions
CREATE TABLE ps_badge_definitions (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    icon_url VARCHAR(255),
    trigger_type VARCHAR(50) NOT NULL,
    threshold DECIMAL(10, 2) NOT NULL,
    tenant_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 9. User Badges (Awards)
CREATE TABLE ps_user_badges (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    badge_id UUID NOT NULL REFERENCES ps_badge_definitions(id) ON DELETE CASCADE,
    awarded_at TIMESTAMP NOT NULL,
    tenant_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_ps_user_badges_user ON ps_user_badges(user_id);
