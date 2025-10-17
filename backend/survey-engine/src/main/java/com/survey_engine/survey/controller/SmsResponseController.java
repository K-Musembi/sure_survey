package com.survey_engine.survey.controller;

import com.survey_engine.survey.common.enums.SurveyStatus;
import com.survey_engine.survey.dto.AnswerRequest;
import com.survey_engine.survey.dto.ResponseRequest;
import com.survey_engine.survey.models.Question;
import com.survey_engine.survey.models.SmsSession;
import com.survey_engine.survey.repository.SurveyRepository;
import com.survey_engine.survey.service.SmsResponseRedisSession;
import com.survey_engine.survey.models.Survey;
import com.survey_engine.survey.service.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sms")
public class SmsResponseController {

    private final SmsResponseRedisSession sessionService;
    private final SurveyRepository surveyRepository;
    private final ResponseService responseService;

    @Autowired
    public SmsResponseController(SmsResponseRedisSession sessionService, SurveyRepository surveyRepository, ResponseService responseService) {
        this.sessionService = sessionService;
        this.surveyRepository = surveyRepository;
        this.responseService = responseService;
    }

    @PostMapping(path = "/inbound", produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> handleSms(@RequestParam("From") String from, @RequestParam("Body") String body) {
        from = from.replaceAll("[^\\d]", ""); // Sanitize phone number
        Optional<SmsSession> sessionOpt = sessionService.getSession(from);

        String responseMessage;
        if (sessionOpt.isEmpty()) {
            responseMessage = handleNewConversation(from, body);
        } else {
            responseMessage = handleOngoingConversation(sessionOpt.get(), body);
        }

        return ResponseEntity.ok(responseMessage);
    }

    private String handleNewConversation(String from, String body) {
        String[] parts = body.trim().split("\\s+");
        if (parts.length != 2 || !parts[0].equalsIgnoreCase("START")) {
            return "To start a survey, please send: START <SURVEY_ID>";
        }

        try {
            Long surveyId = Long.parseLong(parts[1]);
            Survey survey = surveyRepository.findById(surveyId)
                    .orElseThrow(() -> new IllegalArgumentException("Survey not found."));

            if (survey.getStatus() != SurveyStatus.ACTIVE) {
                return "This survey is not currently active.";
            }

            if (survey.getQuestions().isEmpty()) {
                return "This survey has no questions.";
            }

            // Sort questions by position
            List<Question> questions = survey.getQuestions().stream()
                    .sorted(Comparator.comparing(Question::getPosition))
                    .collect(Collectors.toList());

            SmsSession newSession = new SmsSession(from, surveyId, 0, new HashMap<>());
            sessionService.saveSession(newSession);

            return questions.get(0).getQuestionText();

        } catch (NumberFormatException e) {
            return "Invalid SURVEY_ID. Please provide a numeric ID.";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String handleOngoingConversation(SmsSession session, String body) {
        Survey survey = surveyRepository.findById(session.surveyId()).orElse(null);
        if (survey == null) {
            sessionService.deleteSession(session.sessionId());
            return "The survey you were taking is no longer available.";
        }

        List<Question> questions = survey.getQuestions().stream()
                .sorted(Comparator.comparing(Question::getPosition))
                .collect(Collectors.toList());

        // Save the answer to the current question
        Question currentQuestion = questions.get(session.currentQuestionIndex());
        session.answers().put(currentQuestion.getId(), body.trim());

        int nextIndex = session.currentQuestionIndex() + 1;

        if (nextIndex < questions.size()) {
            // There are more questions
            SmsSession updatedSession = new SmsSession(session.sessionId(), session.surveyId(), nextIndex, session.answers());
            sessionService.saveSession(updatedSession);
            return questions.get(nextIndex).getQuestionText();
        } else {
            // This was the last question, survey is complete
            List<AnswerRequest> answerRequests = session.answers().entrySet().stream()
                    .map(entry -> new AnswerRequest(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());

            ResponseRequest finalRequest = new ResponseRequest(answerRequests);
            responseService.createResponse(session.surveyId(), finalRequest, null, session.sessionId()); // participantId is null for SMS

            sessionService.deleteSession(session.sessionId());
            return "Thank you for completing the survey!";
        }
    }
}
