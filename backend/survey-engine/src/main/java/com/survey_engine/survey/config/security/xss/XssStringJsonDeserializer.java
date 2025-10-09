package com.survey_engine.survey.config.security.xss;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import java.io.IOException;

public class XssStringJsonDeserializer extends JsonDeserializer<String> {

    private static final PolicyFactory POLICY_FACTORY = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null) {
            return null;
        }
        return POLICY_FACTORY.sanitize(value);
    }
}
