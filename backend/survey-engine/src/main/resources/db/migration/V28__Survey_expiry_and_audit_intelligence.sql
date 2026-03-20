-- V28: Survey expiry tracking + intelligence action audit

-- Track who viewed / exported intelligence reports (compliance)
CREATE TABLE intelligence_audit_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       BIGINT NOT NULL,
    user_id         BIGINT,
    report_id       UUID REFERENCES insight_reports(id) ON DELETE SET NULL,
    action          VARCHAR(50) NOT NULL,  -- VIEWED | EXPORTED | ACTION_PLAN_UPDATED
    details         TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_intel_audit_tenant ON intelligence_audit_log(tenant_id);
CREATE INDEX idx_intel_audit_report ON intelligence_audit_log(report_id);
