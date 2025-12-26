package com.survey_engine.common.models;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Abstract base class for entities that require multi-tenancy support.
 * Provides common fields like tenant ID, creation timestamp, and update timestamp.
 */
@Data
@MappedSuperclass
public abstract class BaseEntity {

    /**
     * The ID of the tenant this entity belongs to.
     */
    @Column(name = "tenant_id", nullable = true)
    private Long tenantId;

    /**
     * The timestamp when the entity was created.
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * The timestamp when the entity was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}