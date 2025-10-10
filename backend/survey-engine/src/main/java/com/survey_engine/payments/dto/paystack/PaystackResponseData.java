package com.survey_engine.payments.dto.paystack;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaystackResponseData(
        @JsonProperty("authorization_url")
        String authorizationUrl,

        @JsonProperty("access_code")
        String accessCode,

        String reference
) {}
