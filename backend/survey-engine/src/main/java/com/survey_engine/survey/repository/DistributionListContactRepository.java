package com.survey_engine.survey.repository;

import com.survey_engine.survey.models.DistributionListContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DistributionListContactRepository extends JpaRepository<DistributionListContact, UUID> {
}
