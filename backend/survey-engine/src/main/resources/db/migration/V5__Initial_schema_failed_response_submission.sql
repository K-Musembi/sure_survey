-- Create the table to store failed RabbitMQ survey response submissions
CREATE TABLE failed_response_submission (
    id BIGSERIAL PRIMARY KEY,
    payload TEXT NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

COMMENT ON TABLE failed_response_submission IS 'Stores survey response submission payloads that failed to be published to RabbitMQ after all retry attempts.';
COMMENT ON COLUMN failed_response_submission.payload IS 'The JSON representation of the ResponseSubmissionPayload.';
COMMENT ON COLUMN failed_response_submission.error_message IS 'The exception message from the last retry attempt.';
