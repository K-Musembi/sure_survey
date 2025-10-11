package com.survey_engine.user.repository;

import com.survey_engine.user.models.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByCompanyId(Long companyId);

    Optional<Participant> findByPhoneNumber(String phoneNumber);
}