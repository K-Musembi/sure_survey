-- Create surveys table
CREATE TABLE surveys (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    access_type VARCHAR(255) NOT NULL,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create questions table
CREATE TABLE questions (
    id BIGSERIAL PRIMARY KEY,
    survey_id BIGINT NOT NULL REFERENCES surveys(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    question_type VARCHAR(255) NOT NULL,
    options TEXT,
    position INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create responses table
CREATE TABLE responses (
    id BIGSERIAL PRIMARY KEY,
    survey_id BIGINT NOT NULL REFERENCES surveys(id) ON DELETE CASCADE,
    status VARCHAR(255) NOT NULL,
    submission_date TIMESTAMP NOT NULL,
    user_id VARCHAR(255),
    session_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create answers table
CREATE TABLE answers (
    id BIGSERIAL PRIMARY KEY,
    response_id BIGINT NOT NULL REFERENCES responses(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    answer_value TEXT,
    position INT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create templates table
CREATE TABLE templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    sector VARCHAR(255),
    sub_sector VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create template_questions table
CREATE TABLE template_questions (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES templates(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    question_type VARCHAR(255) NOT NULL,
    options TEXT,
    position INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Add indexes for foreign keys to improve performance
CREATE INDEX idx_questions_survey_id ON questions(survey_id);
CREATE INDEX idx_responses_survey_id ON responses(survey_id);
CREATE INDEX idx_answers_response_id ON answers(response_id);
CREATE INDEX idx_answers_question_id ON answers(question_id);
CREATE INDEX idx_template_questions_template_id ON template_questions(template_id);
