package com.survey_engine.survey.template;

import com.survey_engine.survey.common.enums.SurveyType;
import com.survey_engine.survey.template.dto.TemplateRequest;
import com.survey_engine.survey.template.dto.TemplateResponse;
import com.survey_engine.survey.template_question.TemplateQuestion;
import com.survey_engine.survey.template_question.dto.TemplateQuestionResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for the Template entity.
 * Defines business logic for managing survey templates.
 */
@Service
public class TemplateService {

    private final TemplateRepository templateRepository;

    /**
     * Constructor for TemplateService.
     * @param templateRepository An instance of TemplateRepository.
     */
    @Autowired
    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    /**
     * Creates a new template. (Admin only)
     * @param request The DTO containing the template data.
     * @param roles The roles of the user making the request.
     * @return A DTO for the created template.
     */
    @Transactional
    public TemplateResponse createTemplate(TemplateRequest request, List<String> roles) {
        if (roles == null || !roles.contains("ADMIN")) {
            throw new AccessDeniedException("Only admins can create templates.");
        }
        if (templateRepository.findByName(request.name()).isPresent()) {
            throw new DataIntegrityViolationException("A template with this name already exists.");
        }

        Template template = new Template();
        Template savedTemplate = getTemplate(template, request);
        return mapToResponse(savedTemplate);
    }

    /**
     * Updates an existing template. (Admin only)
     * @param id The ID of the template to update.
     * @param request The DTO with updated data.
     * @param roles The roles of the user making the request.
     * @return A DTO for the updated template.
     */
    @Transactional
    public TemplateResponse updateTemplate(Long id, TemplateRequest request, List<String> roles) {
        if (roles == null || !roles.contains("ADMIN")) {
            throw new AccessDeniedException("Only admins can update templates.");
        }
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " + id));

        Template savedTemplate = getTemplate(template, request);
        return mapToResponse(savedTemplate);
    }

    /**
     * Deletes a template. (Admin only)
     * @param id The ID of the template to delete.
     * @param roles The roles of the user making the request.
     */
    @Transactional
    public void deleteTemplate(Long id, List<String> roles) {
        if (roles == null || !roles.contains("ADMIN")) {
            throw new AccessDeniedException("Only admins can delete templates.");
        }
        if (!templateRepository.existsById(id)) {
            throw new EntityNotFoundException("Template not found with id: " + id);
        }
        templateRepository.deleteById(id);
    }

    /**
     * Retrieves all templates.
     * @return A list of all template DTOs.
     */
    @Transactional(readOnly = true)
    public List<TemplateResponse> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single template by its ID.
     * @param id The ID of the template.
     * @return A template DTO.
     */
    @Transactional(readOnly = true)
    public TemplateResponse getTemplateById(Long id) {
        return templateRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " + id));
    }

    /**
     * Retrieves all templates of a specific type.
     * @param type The survey type.
     * @return A list of template DTOs.
     */
    @Transactional(readOnly = true)
    public List<TemplateResponse> getTemplatesByType(SurveyType type) {
        return templateRepository.findByType(type).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all templates for a specific sector.
     * @param sector The industry sector.
     * @return A list of template DTOs.
     */
    @Transactional(readOnly = true)
    public List<TemplateResponse> getTemplatesBySector(String sector) {
        return templateRepository.findBySector(sector).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Template getTemplate(Template template, TemplateRequest request) {
        template.setName(request.name());
        template.setType(request.type());
        template.setSector(request.sector());
        template.setSubSector(request.subSector());

        if (template.getQuestions() != null) {
            template.getQuestions().clear();
        }
        List<TemplateQuestion> questions = request.questions().stream().map(qRequest -> {
            TemplateQuestion question = new TemplateQuestion();
            question.setTemplate(template);
            question.setQuestionText(qRequest.questionText());
            question.setQuestionType(qRequest.questionType());
            question.setOptions(qRequest.options());
            question.setPosition(qRequest.position());
            return question;
        }).toList();
        template.getQuestions().addAll(questions);

        return templateRepository.save(template);
    }

    private TemplateResponse mapToResponse(Template template) {
        List<TemplateQuestionResponse> questionResponses = template.getQuestions().stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());

        return new TemplateResponse(
                template.getId(),
                template.getName(),
                template.getType(),
                template.getSector(),
                template.getSubSector(),
                questionResponses,
                template.getCreatedAt()
        );
    }

    private TemplateQuestionResponse mapToQuestionResponse(TemplateQuestion question) {
        return new TemplateQuestionResponse(
                question.getId(),
                question.getQuestionText(),
                question.getQuestionType(),
                question.getOptions(),
                question.getPosition()
        );
    }
}
