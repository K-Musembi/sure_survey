package com.survey_engine.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends SurveyPlatformException {

    public ResourceNotFoundException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.NOT_FOUND);
    }
}
