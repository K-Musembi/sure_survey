package com.survey_engine.survey.template;

import com.survey_engine.survey.common.enums.SurveyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for the Template entity.
 */
@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {

    /**
     * Finds a template by its name.
     * @param name The name of the template.
     * @return An Optional containing the template if found.
     */
    Optional<Template> findByName(String name);

    /**
     * Finds all templates of a specific type.
     * @param type The type of the survey.
     * @return A list of templates.
     */
    List<Template> findByType(SurveyType type);

    /**
     * Finds all templates for a specific sector.
     * @param sector The industry sector.
     * @return A list of templates.
     */
    List<Template> findBySector(String sector);
}
