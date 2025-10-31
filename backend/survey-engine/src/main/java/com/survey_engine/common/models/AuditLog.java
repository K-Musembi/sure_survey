package com.survey_engine.common.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents an audit log entry for tracking significant user actions.
 * This entity maps to the 'audit_log' table in the database.
 */
@Entity
@Table(name = "audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    /**
     * The unique identifier for the audit log entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The identifier of the user who performed the action.
     */
    @Column(name = "actor", nullable = false)
    private String actor;

    /**
     * A description of the action performed.
     */
    @Column(name = "action", nullable = false)
    private String action;

    /**
     * Additional parameters or context related to the action, stored as a JSON string.
     */
    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters;

    /**
     * The status of the audited action (e.g., SUCCESS, FAILURE).
     */
    @Column(name = "status", nullable = false)
    private String status;

    /**
     * The timestamp when the audit log entry was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}