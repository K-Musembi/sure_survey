package com.survey_engine.user.config.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.UUID;


/**
 * Configures the Spring Security OAuth2 Authorization Server and provides JWT generation capabilities.
 * This class is responsible for setting up the security filter chain for authorization endpoints,
 * registering OAuth2 clients, and providing essential beans for JWT handling and server settings.
 */
@Configuration
@RequiredArgsConstructor
public class JwtAuthorization {

    private final TenantResolverFilter tenantResolverFilter;

    @Value("${jwt.keystore.path}")
    private String keystorePath;

    @Value("${jwt.keystore.password}")
    private String keystorePassword;

    @Value("${jwt.keystore.alias}")
    private String keyAlias;

    /**
     * Creates and configures the primary {@link SecurityFilterChain} for the OAuth2 Authorization Server endpoints.
     * This chain is given high precedence with {@code @Order(1)} to ensure it acts before the default security filter chain.
     *
     * @param http The {@link HttpSecurity} to configure.
     * @return The configured {@link SecurityFilterChain}.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {

        http.addFilterBefore(tenantResolverFilter, UsernamePasswordAuthenticationFilter.class);

        // 1. Initialize the configurer and create a request matcher for the endpoints.
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

        // 2. Apply the configuration using `http.with()` and disable CSRF for the endpoints.
        http
                .securityMatcher(endpointsMatcher)
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
                .with(authorizationServerConfigurer, Customizer.withDefaults());

        // 3. Configure OIDC (as in the original code)
        authorizationServerConfigurer.oidc(Customizer.withDefaults());

        // 4. Handle exceptions, returning a 401 for unauthorized API clients.
        http.exceptionHandling(exceptions ->
                exceptions.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        );

        // 5. Configure the resource server for the authorization server endpoints.
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    /**
     * Provides a {@link PasswordEncoder} bean for encoding and verifying passwords.
     * @return A {@link BCryptPasswordEncoder} instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines the set of OAuth2 clients that are permitted to use this authorization server.
     * This implementation uses an in-memory repository with a single, hardcoded client,
     * suitable for development and demonstration purposes.
     *
     * @return A {@link RegisteredClientRepository} containing the configured clients.
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder) {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("client")
                .clientSecret(passwordEncoder.encode("secret")) // Encode the secret
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUri("http://127.0.0.1:8080/login/oauth2/code/client-oidc")
                .scope(OidcScopes.OPENID)
                .scope("read")
                .scope("write")
                .clientSettings(ClientSettings.builder().requireProofKey(true).requireAuthorizationConsent(false).build())
                .build();

        return new InMemoryRegisteredClientRepository(registeredClient);
    }

    /**
     * Provides a {@link JwtDecoder} bean for decoding and validating JWTs.
     * This is used by the resource server components to verify access tokens.
     *
     * @param jwkSource The {@link JWKSource} containing the public keys for verifying JWT signatures.
     * @return A {@link JwtDecoder} instance.
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * Provides global settings for the authorization server, such as the issuer URL.
     * The issuer URL is a critical claim embedded in the generated JWTs.
     *
     * @return An {@link AuthorizationServerSettings} instance.
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://127.0.0.1:8080")
                .build();
    }

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

