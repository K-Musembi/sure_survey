package com.survey.survey.template_question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for the TemplateQuestion entity.
 */
@Repository
public interface TemplateQuestionRepository extends JpaRepository<TemplateQuestion, Long> {

    /**
     * Finds all TemplateQuestions associated with a specific Template.
     * @param templateId The ID of the Template.
     * @return A list of TemplateQuestions.
     */
    List<TemplateQuestion> findByTemplateId(Long templateId);
}
