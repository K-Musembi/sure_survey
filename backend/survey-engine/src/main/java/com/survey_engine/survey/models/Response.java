package com.survey_engine.survey.models;

import com.survey_engine.survey.common.enums.ResponseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ResponseStatus status;

    @Column(name = "submission_date", nullable = false)
    private LocalDateTime submissionDate;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "session_id")
    private String sessionId;

    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (submissionDate == null) {
            submissionDate = createdAt;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}