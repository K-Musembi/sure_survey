package com.survey_engine.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a domain / business rule is violated.
 * Examples: referral rate limit exceeded, survey already closed, invalid state transition.
 */
public class BusinessRuleException extends SurveyPlatformException {

    public BusinessRuleException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.BAD_REQUEST);
    }
}
