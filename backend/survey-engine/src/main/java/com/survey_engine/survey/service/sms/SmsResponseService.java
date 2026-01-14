package com.survey_engine.survey.service.sms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey_engine.survey.common.enums.SurveyStatus;
import com.survey_engine.survey.dto.AnswerRequest;
import com.survey_engine.survey.dto.ResponseRequest;
import com.survey_engine.survey.models.Question;
import com.survey_engine.survey.dto.sms.SmsRedisSession;
import com.survey_engine.survey.models.Survey;
import com.survey_engine.survey.repository.SurveyRepository;
import com.survey_engine.survey.service.ResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class to handle the business logic for incoming SMS survey responses.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmsResponseService {

    private final SmsResponseRedisSession sessionService;
    private final SurveyRepository surveyRepository;
    private final ResponseService responseService;
    private final SmsSendingService smsSendingService;

    /**
     * Initiates a survey session for a specific user (phone number) via push (outbound).
     * This is used when sending surveys to a distribution list or triggered by an event.
     *
     * @param phoneNumber The recipient's phone number.
     * @param surveyId    The ID of the survey to start.
     */
    public void initiateSurvey(String phoneNumber, Long surveyId) {
        initiateSurvey(phoneNumber, surveyId, new HashMap<>());
    }

    /**
     * Initiates a survey session with additional context.
     *
     * @param phoneNumber The recipient's phone number.
     * @param surveyId    The ID of the survey to start.
     * @param context     Additional metadata/context for the session (e.g., subject attribution).
     */
    public void initiateSurvey(String phoneNumber, Long surveyId, Map<String, String> context) {
        try {
            Survey survey = surveyRepository.findById(surveyId)
                    .orElseThrow(() -> new IllegalArgumentException("Survey not found."));

            if (survey.getStatus() != SurveyStatus.ACTIVE) {
                log.warn("Attempted to initiate survey {} which is not ACTIVE.", surveyId);
                return;
            }

            if (survey.getQuestions().isEmpty()) {
                log.warn("Attempted to initiate survey {} which has no questions.", surveyId);
                return;
            }

            // Sort questions by position
            List<Question> questions = survey.getQuestions().stream()
                    .sorted(Comparator.comparing(Question::getPosition))
                    .toList();

            // Create new session with context
            SmsRedisSession newSession = new SmsRedisSession(phoneNumber, surveyId, 0, new HashMap<>(), context);
            sessionService.saveSession(newSession);

            // Send first question
            String firstQuestion = questions.get(0).getQuestionText();
            smsSendingService.sendSms(phoneNumber, firstQuestion);

        } catch (Exception e) {
            log.error("Failed to initiate survey for {}: {}", phoneNumber, e.getMessage(), e);
        }
    }

    /**
     * Main entry point for handling an incoming SMS message.
     * It checks for an existing session and delegates to the appropriate handler.
     *
     * @param from The sanitized phone number of the sender.
     * @param body The content of the SMS message.
     * @return The appropriate response message to send back to the user.
     */
    public String handleSmsRequest(String from, String body) {
        Optional<SmsRedisSession> sessionOpt = sessionService.getSession(from);

        if (sessionOpt.isEmpty()) {
            return handleNewConversation(from, body);
        } else {
            return handleOngoingConversation(sessionOpt.get(), body);
        }
    }

    /**
     * Handles the first message from a user to start a new survey session.
     *
     * @param from The sender's phone number.
     * @param body The message body, expected to be in the format START <SURVEY_ID>.
     * @return The first question of the survey or an error message.
     */
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
                    .toList();

            // Create new session (no context for self-initiated)
            SmsRedisSession newSession = new SmsRedisSession(from, surveyId, 0, new HashMap<>(), new HashMap<>());
            sessionService.saveSession(newSession);

            return questions.get(0).getQuestionText();

        } catch (NumberFormatException e) {
            return "Invalid SURVEY_ID. Please provide a numeric ID.";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    /**
     * Handles subsequent messages in an ongoing survey conversation.
     *
     * @param session The current {@link SmsRedisSession} for the user.
     * @param body The user's answer to the previous question.
     * @return The next question, a "thank you" message, or an error message.
     */
    private String handleOngoingConversation(SmsRedisSession session, String body) {
        Survey survey = surveyRepository.findById(session.surveyId()).orElse(null);
        if (survey == null) {
            sessionService.deleteSession(session.sessionId());
            return "The survey you were taking is no longer available.";
        }

        List<Question> questions = survey.getQuestions().stream()
                .sorted(Comparator.comparing(Question::getPosition))
                .toList();

        // Save the answer to the current question
        Question currentQuestion = questions.get(session.currentQuestionIndex());
        session.answers().put(currentQuestion.getId(), body.trim());

        int nextIndex = session.currentQuestionIndex() + 1;

        if (nextIndex < questions.size()) {
            // There are more questions
            SmsRedisSession updatedSession = new SmsRedisSession(session.sessionId(), session.surveyId(), nextIndex, session.answers(), session.context());
            sessionService.saveSession(updatedSession);
            return questions.get(nextIndex).getQuestionText();
        } else {
            // This was the last question, survey is complete
            List<AnswerRequest> answerRequests = session.answers().entrySet().stream()
                    .map(entry -> new AnswerRequest(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());

            ResponseRequest finalRequest = new ResponseRequest(answerRequests, session.context());
            
            // Pass the context as metadata
            responseService.createResponse(session.surveyId(), finalRequest, null, session.sessionId(), session.context()); 

            sessionService.deleteSession(session.sessionId());
            return "Thank you for completing the survey!";
        }
    }
}
