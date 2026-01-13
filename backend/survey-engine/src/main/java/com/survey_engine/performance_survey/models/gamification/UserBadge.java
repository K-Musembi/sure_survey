package com.survey_engine.performance_survey.models.gamification;

import com.survey_engine.common.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ps_user_badges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserBadge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private BadgeDefinition badge;

    @Column(name = "awarded_at", nullable = false)
    private LocalDateTime awardedAt;
}