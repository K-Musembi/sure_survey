package com.survey_engine.common.config.security;


import com.survey_engine.user.UserApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import java.io.InputStream;
import java.security.KeyStore;


/**
 * Security configuration file for the application.
 * Configures authentication, authorization, and JWT handling.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Path to the JWT keystore file.
     */
    @Value("${jwt.keystore.path}")
    private String keystorePath;

    /**
     * Password for the JWT keystore.
     */
    @Value("${jwt.keystore.password}")
    private String keystorePassword;

    /**
     * Alias of the key within the keystore.
     */
    @Value("${jwt.keystore.alias}")
    private String keyAlias;

    /**
     * Configures the security filter chain for HTTP requests.
     *
     * @param http The {@link HttpSecurity} to configure.
     * @return A configured {@link SecurityFilterChain}.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // allow CORS preflight requests
                        .requestMatchers("/api/v1/auth/signup", "/api/v1/auth/login").permitAll() // Permit login endpoint
                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    /**
     * Provides a {@link PasswordEncoder} bean for encoding and verifying passwords.
     *
     * @return A {@link BCryptPasswordEncoder} instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Provides a {@link UserDetailsService} bean that loads user-specific data.
     * It uses the {@link UserApi} to find user details by email.
     *
     * @param userApi The {@link UserApi} instance for user data access.
     * @return A {@link UserDetailsService} implementation.
     */
    @Bean
    public UserDetailsService userDetailsService(UserApi userApi) {
        return email -> userApi.findUserDetailsByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Provides an {@link AuthenticationProvider} bean for authenticating users.
     *
     * @param userDetailsService The {@link UserDetailsService} to retrieve user details.
     * @param passwordEncoder The {@link PasswordEncoder} to verify passwords.
     * @return A {@link DaoAuthenticationProvider} instance.
     */
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * Provides an {@link AuthenticationManager} bean.
     *
     * @param config The {@link AuthenticationConfiguration}.
     * @return An {@link AuthenticationManager} instance.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Provides a {@link JwtEncoder} bean for encoding JWTs.
     * It uses the JWK source configured in {@link JwtAuthorization}.
     *
     * @param jwkSource The {@link JWKSource} for signing JWTs.
     * @return A {@link NimbusJwtEncoder} instance.
     */
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }
}