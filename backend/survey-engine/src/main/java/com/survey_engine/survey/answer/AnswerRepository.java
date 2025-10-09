package com.survey_engine.survey.answer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for the Answer entity.
 */
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    /**
     * Finds all Answers associated with a specific Response.
     * @param responseId The ID of the Response.
     * @return A list of Answers.
     */
    List<Answer> findByResponseId(Long responseId);
}
