package com.survey_engine.referral.domain;

import com.survey_engine.referral.domain.enums.RequestStatus;
import com.survey_engine.referral.domain.enums.RequestType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ODPC-compliant record of a Data Subject Request (SAR or Erasure).
 * Phone is never stored raw — only as a SHA-256 hash.
 */
@Entity
@Table(name = "data_subject_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSubjectRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, updatable = false)
    private RequestType requestType;

    @Column(name = "phone_hash", nullable = false, updatable = false)
    private String phoneHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status = RequestStatus.RECEIVED;

    @Column(name = "notes")
    private String notes;

    @Column(name = "tenant_id", updatable = false)
    private Long tenantId;

    @Column(name = "requested_at", updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() { requestedAt = LocalDateTime.now(); }
}
