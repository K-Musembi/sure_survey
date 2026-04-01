package com.survey_engine.survey.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "survey_consent_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyConsentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "survey_id", nullable = false)
    private Long surveyId;

    @Column(name = "phone_hash", length = 64)
    private String phoneHash;

    @Column(name = "participant_id")
    private String participantId;

    @Column(name = "event_type", nullable = false, length = 20)
    private String eventType; // OPT_IN | OPT_OUT

    @Column(name = "channel", nullable = false, length = 20)
    private String channel; // SMS | WEB | USSD | WHATSAPP

    @Column(name = "consent_message", columnDefinition = "TEXT")
    private String consentMessage;

    @Column(name = "privacy_notice_url", length = 500)
    private String privacyNoticeUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
