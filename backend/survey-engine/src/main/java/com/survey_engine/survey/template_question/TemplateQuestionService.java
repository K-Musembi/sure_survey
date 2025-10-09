package com.survey.survey.template_question;

import com.survey.survey.template.Template;
import com.survey.survey.template.TemplateRepository;
import com.survey.survey.template_question.dto.TemplateQuestionRequest;
import com.survey.survey.template_question.dto.TemplateQuestionResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for the TemplateQuestion entity.
 * Defines business logic for managing questions within a template.
 */
@Service
public class TemplateQuestionService {

    private final TemplateQuestionRepository templateQuestionRepository;
    private final TemplateRepository templateRepository;

    /**
     * Constructor for TemplateQuestionService.
     * @param templateQuestionRepository An instance of TemplateQuestionRepository.
     * @param templateRepository An instance of TemplateRepository.
     */
    @Autowired
    public TemplateQuestionService(TemplateQuestionRepository templateQuestionRepository, TemplateRepository templateRepository) {
        this.templateQuestionRepository = templateQuestionRepository;
        this.templateRepository = templateRepository;
    }

    /**
     * Creates a new question for a given template. (Admin only)
     * @param templateId The ID of the template.
     * @param request The DTO with question data.
     * @param roles The roles of the user making the request.
     * @return A DTO for the created question.
     */
    @Transactional
    public TemplateQuestionResponse createTemplateQuestion(Long templateId, TemplateQuestionRequest request, List<String> roles) {
        if (roles == null || !roles.contains("ADMIN")) {
            throw new AccessDeniedException("Only admins can create template questions.");
        }
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " + templateId));

        TemplateQuestion question = new TemplateQuestion();
        question.setTemplate(template);
        question.setQuestionText(request.questionText());
        question.setQuestionType(request.questionType());
        question.setOptions(request.options());
        question.setPosition(request.position());

        TemplateQuestion savedQuestion = templateQuestionRepository.save(question);
        return mapToResponse(savedQuestion);
    }

    /**
     * Updates an existing template question. (Admin only)
     * @param questionId The ID of the question to update.
     * @param request The DTO with updated data.
     * @param roles The roles of the user making the request.
     * @return A DTO for the updated question.
     */
    @Transactional
    public TemplateQuestionResponse updateTemplateQuestion(Long questionId, TemplateQuestionRequest request, List<String> roles) {
        if (roles == null || !roles.contains("ADMIN")) {
            throw new AccessDeniedException("Only admins can update template questions.");
        }
        TemplateQuestion question = templateQuestionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("TemplateQuestion not found with id: " + questionId));

        question.setQuestionText(request.questionText());
        question.setQuestionType(request.questionType());
        question.setOptions(request.options());
        question.setPosition(request.position());

        TemplateQuestion updatedQuestion = templateQuestionRepository.save(question);
        return mapToResponse(updatedQuestion);
    }

    /**
     * Deletes a template question. (Admin only)
     * @param questionId The ID of the question to delete.
     * @param roles The roles of the user making the request.
     */
    @Transactional
    public void deleteTemplateQuestion(Long questionId, List<String> roles) {
        if (roles == null || !roles.contains("ADMIN")) {
            throw new AccessDeniedException("Only admins can delete template questions.");
        }
        if (!templateQuestionRepository.existsById(questionId)) {
            throw new EntityNotFoundException("TemplateQuestion not found with id: " + questionId);
        }
        templateQuestionRepository.deleteById(questionId);
    }

    /**
     * Retrieves all questions for a given template.
     * @param templateId The ID of the template.
     * @return A list of question DTOs.
     */
    @Transactional(readOnly = true)
    public List<TemplateQuestionResponse> getQuestionsByTemplateId(Long templateId) {
        if (!templateRepository.existsById(templateId)) {
            throw new EntityNotFoundException("Template not found with id: " + templateId);
        }
        return templateQuestionRepository.findByTemplateId(templateId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single template question by its ID.
     * @param questionId The ID of the question.
     * @return A question DTO.
     */
    @Transactional(readOnly = true)
    public TemplateQuestionResponse getQuestionById(Long questionId) {
        return templateQuestionRepository.findById(questionId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("TemplateQuestion not found with id: " + questionId));
    }

    /**
     * Maps a TemplateQuestion entity to a TemplateQuestionResponse DTO.
     * @param question The entity to map.
     * @return The response DTO.
     */
    private TemplateQuestionResponse mapToResponse(TemplateQuestion question) {
        return new TemplateQuestionResponse(
                question.getId(),
                question.getQuestionText(),
                question.getQuestionType(),
                question.getOptions(),
                question.getPosition()
        );
    }
}
