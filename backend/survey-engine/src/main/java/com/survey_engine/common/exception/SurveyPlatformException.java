package com.survey_engine.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for all business-logic errors in the platform.
 * Every module must throw a subclass of this rather than using raw
 * IllegalStateException or IllegalArgumentException for domain errors.
 *
 * Error codes follow the pattern: MODULE_NOUN_CONDITION
 * e.g. SURVEY_BRANCH_RULE_NOT_FOUND, REFERRAL_INVITE_LIMIT_EXCEEDED
 */
public class SurveyPlatformException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    public SurveyPlatformException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public SurveyPlatformException(String errorCode, String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
