package com.survey_engine.survey.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "distribution_list_contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DistributionListContact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distribution_list_id", nullable = false)
    private DistributionList distributionList;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;
}
