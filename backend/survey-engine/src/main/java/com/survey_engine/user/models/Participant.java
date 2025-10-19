package com.survey_engine.user.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a participant in a survey or reward program.
 * This entity stores contact information for individuals who may not be full system users.
 */
@Entity
@Table(name = "participants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Participant {

    /**
     * The unique identifier for the participant.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The full name of the participant.
     */
    @Column(name = "full_name")
    private String fullName;

    /**
     * The unique phone number of the participant.
     */
    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    /**
     * The unique email address of the participant.
     */
    @Column(name = "email", unique = true)
    private String email;

    /**
     * The timestamp when the participant record was created.
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * The timestamp when the participant record was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Sets creation and update timestamps before persisting the entity.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the timestamp before updating the entity.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
