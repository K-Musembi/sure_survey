package com.survey_engine.intelligence.domain;

import com.survey_engine.intelligence.domain.enums.ReportStatus;
import com.survey_engine.intelligence.domain.enums.ReportType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "insight_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsightReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "survey_id")
    private Long surveyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    @Column(name = "title")
    private String title;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "executive_summary", columnDefinition = "TEXT")
    private String executiveSummary;

    /** JSON array: [{type, text, area}] */
    @Column(name = "key_findings", columnDefinition = "TEXT")
    private String keyFindings;

    /** JSON array: [{priority, area, recommendedAction, suggestedOwner, suggestedTimeline}] */
    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    /** JSON array: [{name, size, description}] */
    @Column(name = "respondent_clusters", columnDefinition = "TEXT")
    private String respondentClusters;

    @Column(name = "response_count")
    private Integer responseCount;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
