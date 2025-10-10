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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class JwtAuthorization {

    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.keystore.path}")
    private String keystorePath;

    @Value("${jwt.keystore.password}")
    private String keystorePassword;

    @Value("${jwt.keystore.alias}")
    private String keyAlias;

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {

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

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("client")
                .clientSecret(passwordEncoder.encode("secret")) // Encode the secret
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                // NOTE: The password grant type is deprecated in OAuth 2.1
                // and should be replaced with a more secure flow like PKCE in the future.
                .authorizationGrantType(new AuthorizationGrantType("password"))
                .redirectUri("http://127.0.0.1:8080/login/oauth2/code/client-oidc")
                .scope(OidcScopes.OPENID)
                .scope("read")
                .scope("write")
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                .build();

        return new InMemoryRegisteredClientRepository(registeredClient);
    }

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

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://127.0.0.1:8080")
                .build();
    }
}
