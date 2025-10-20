package com.survey_engine.common.config.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.InputStream;
import java.security.KeyStore;

@Configuration
public class JwtConfig {

    @Value("${jwt.keystore.path}")
    private String keystorePath;

    @Value("${jwt.keystore.password}")
    private String keystorePassword;

    @Value("${jwt.keystore.alias}")
    private String keyAlias;

    /**
     * Provides a {@link JWKSource} for signing JWTs.
     * @return A {@link JWKSource} instance.
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(keystorePath)) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(is, keystorePassword.toCharArray());
            RSAKey rsaKey = RSAKey.load(keyStore, keyAlias, keystorePassword.toCharArray());
            JWKSet jwkSet = new JWKSet(rsaKey);
            return new ImmutableJWKSet<>(jwkSet);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load JWK source", ex);
        }
    }

    /**
     * Provides a {@link JwtEncoder} bean for encoding JWTs.
     *
     * @param jwkSource The {@link JWKSource} for signing JWTs.
     * @return A {@link NimbusJwtEncoder} instance.
     */
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {

        return new NimbusJwtEncoder(jwkSource);
    }
}
