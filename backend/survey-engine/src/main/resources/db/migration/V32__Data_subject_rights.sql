-- V32: ODPC Data Subject Rights — Subject Access Requests (SAR) and Erasure Requests.
-- Provides audit trail for DSR compliance under the Kenya Data Protection Act 2019.

CREATE TABLE data_subject_requests (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_type    VARCHAR(10)  NOT NULL CHECK (request_type IN ('ACCESS', 'ERASURE')),
    phone_hash      VARCHAR(64)  NOT NULL,   -- SHA-256(phone + salt) — never store raw phone
    status          VARCHAR(15)  NOT NULL DEFAULT 'RECEIVED'
                        CHECK (status IN ('RECEIVED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED')),
    notes           TEXT,                    -- Admin notes / rejection reason
    tenant_id       BIGINT,                  -- NULL means cross-tenant (global) request
    requested_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMP
);

CREATE INDEX idx_dsr_phone_hash      ON data_subject_requests(phone_hash);
CREATE INDEX idx_dsr_status          ON data_subject_requests(status);
CREATE INDEX idx_dsr_requested_at    ON data_subject_requests(requested_at);
