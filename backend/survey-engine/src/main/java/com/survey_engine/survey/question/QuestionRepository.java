package com.survey.survey.question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Question entity
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * Method to find all questions by survey id
     * @param surveyId - survey id
     * @return - List of questions
     */
    List<Question> findBySurveyId(Long surveyId);
}