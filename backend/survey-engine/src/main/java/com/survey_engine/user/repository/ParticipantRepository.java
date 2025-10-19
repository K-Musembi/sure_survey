package com.survey_engine.user.repository;

import com.survey_engine.user.models.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for the {@link Participant} entity.
 */
@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    /**
     * Finds a {@link Participant} by their phone number.
     *
     * @param phoneNumber The phone number to search for.
     * @return An {@link Optional} containing the found participant, or empty if not found.
     */
    Optional<Participant> findByPhoneNumber(String phoneNumber);
}