-- V24: Survey branching logic, per-question scoring, and completion milestones

-- Add scoring fields to questions (nullable — backwards compatible)
ALTER TABLE questions
    ADD COLUMN weight       DECIMAL(5,2)  DEFAULT 1.0,
    ADD COLUMN score_map    TEXT,          -- JSON: {"0": 0, "1": 5, "2": 10}  (option index → score)
    ADD COLUMN category     VARCHAR(100);  -- Groups questions for category-level scoring

-- Branch rules define the conditional flow of a survey (DAG)
CREATE TABLE branch_rules (
    id                  BIGSERIAL PRIMARY KEY,
    survey_id           BIGINT NOT NULL REFERENCES surveys(id) ON DELETE CASCADE,
    source_question_id  BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    condition_type      VARCHAR(30) NOT NULL,
    -- ANSWER_EQUALS | SCORE_LT | SCORE_GT | SCORE_CATEGORY_LT | SCORE_CATEGORY_GT | ALWAYS
    condition_value     TEXT,
    -- JSON: {"optionIndex":2} | {"threshold":0.6} | {"threshold":0.4,"category":"Compliance"}
    target_question_id  BIGINT REFERENCES questions(id) ON DELETE SET NULL,
    -- NULL means end the survey at this branch
    priority            INT NOT NULL DEFAULT 0,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_branch_rules_survey ON branch_rules(survey_id);
CREATE INDEX idx_branch_rules_source ON branch_rules(source_question_id);

-- Survey milestones: progress checkpoints shown during a survey session
CREATE TABLE survey_milestones (
    id              BIGSERIAL PRIMARY KEY,
    survey_id       BIGINT NOT NULL REFERENCES surveys(id) ON DELETE CASCADE,
    threshold_pct   INT NOT NULL,   -- 25 | 50 | 75 | 100
    message         TEXT,           -- e.g. "You're halfway there! Keep going."
    badge_type_id   UUID,  -- References a badge definition (no FK; table managed separately)
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (survey_id, threshold_pct)
);

CREATE INDEX idx_survey_milestones_survey ON survey_milestones(survey_id);
