package com.survey_engine.survey.dto.sms;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents the state of an SMS survey session stored in Redis.
 * This object is used to track a user's progress through a survey via a stateless protocol.
 *
 * @param sessionId The user's phone number, acting as the session identifier.
 * @param surveyId The ID of the survey being taken.
 * @param currentQuestionIndex The 0-based index of the current question being asked.
 * @param answers A map storing the question ID and the answer value provided by the user.
 * @param context A map for storing additional metadata/context (e.g., subject attribution) to be persisted with the response.
 */
public record SmsRedisSession(
        String sessionId,
        Long surveyId,
        int currentQuestionIndex,
        Map<Long, String> answers,
        Map<String, String> context
) implements Serializable {}