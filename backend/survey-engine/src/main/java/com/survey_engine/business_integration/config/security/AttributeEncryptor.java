package com.survey_engine.business_integration.config.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

@Component
@Converter
public class AttributeEncryptor implements AttributeConverter<String, String> {

    private final TextEncryptor textEncryptor;

    public AttributeEncryptor(@Value("${app.encryption.password}") String password,
                              @Value("${app.encryption.salt}") String salt) {
        this.textEncryptor = Encryptors.text(password, salt);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        return textEncryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return textEncryptor.decrypt(dbData);
    }
}
