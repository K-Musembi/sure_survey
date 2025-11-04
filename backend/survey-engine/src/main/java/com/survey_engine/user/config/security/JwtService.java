package com.survey_engine.user.config.security;

import com.survey_engine.user.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

/**
 * Service for generating JSON Web Tokens (JWTs).
 * Uses Spring Security's JwtEncoder to create signed JWTs with custom claims.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    /**
     * Configured expiration time for JWTs in minutes.
     */
    @Value("${jwt.expiration}")
    private long jwtExpiration; // in minutes

    /**
     * Generates a JWT for the authenticated user.
     *
     * @param authentication The {@link Authentication} object representing the authenticated user.
     * @return A signed JWT token string.
     */
    public String generateToken(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Instant now = Instant.now();
        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(jwtExpiration, ChronoUnit.MINUTES))
                .subject(user.getId().toString())
                .claim("scope", scope)
                .claim("tenantId", user.getTenantId())
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}