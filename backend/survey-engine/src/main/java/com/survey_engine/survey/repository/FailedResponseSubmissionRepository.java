package com.survey_engine.survey.repository;

import com.survey_engine.survey.models.FailedResponseSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FailedResponseSubmissionRepository extends JpaRepository<FailedResponseSubmission, Long> {
}