package com.survey_engine.common.repository;

import com.survey_engine.common.models.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for the {@link AuditLog} entity.
 * Provides CRUD operations for audit log entries.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}