package com.survey_engine.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a call to an external service (SMS gateway, AI API, payment provider) fails.
 * The caller must not silently swallow this — let it propagate to the global handler.
 */
public class ExternalServiceException extends SurveyPlatformException {

    public ExternalServiceException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, HttpStatus.BAD_GATEWAY, cause);
    }
}
