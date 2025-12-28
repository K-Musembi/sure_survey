package com.survey_engine.user.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Model class for Tenant entity
 * Maps to database table using JPA Hibernate ORM
 */
@Entity
@Table(name = "tenants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

    /**
     * The unique identifier for the tenant.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * A unique, URL-friendly identifier for the tenant (e.g., subdomain).
     */
    @Column(unique = true)
    private String slug;

    /**
     * The human-readable name of the tenant.
     */
    @Column(nullable = false)
    private String name;

    /**
     * The current status of the tenant (e.g., ACTIVE, INACTIVE).
     */
    @Column(nullable = false)
    private String status;

    /**
     * S3 key for tenant-specific branding assets.
     */
    @Column
    private String branding_s3_key;

    /**
     * Subscription id
     */
    @Column(name = "subscription_id")
    private UUID subscriptionId;

    /**
     * List of users belonging to this tenant.
     */
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users = new ArrayList<>();

    /**
     * The timestamp when the tenant was created.
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * The timestamp when the tenant was last updated.
     */
    @Column(name = "updated_at", updatable = true)
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