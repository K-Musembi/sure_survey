package com.survey_engine.survey.service;

import com.survey_engine.survey.common.enums.SurveyType;
import com.survey_engine.survey.models.Template;
import com.survey_engine.survey.repository.TemplateRepository;
import com.survey_engine.survey.dto.TemplateRequest;
import com.survey_engine.survey.dto.TemplateResponse;
import com.survey_engine.survey.models.TemplateQuestion;
import com.survey_engine.survey.dto.TemplateQuestionResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TemplateService {

    private final TemplateRepository templateRepository;

    @Autowired
    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

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

    @Transactional(readOnly = true)
    public List<TemplateResponse> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TemplateResponse getTemplateById(Long id) {
        return templateRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> getTemplatesByType(SurveyType type) {
        return templateRepository.findByType(type).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

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
