package com.survey_engine.user.service;

import com.survey_engine.common.exception.BusinessRuleException;
import com.survey_engine.user.models.User;
import com.survey_engine.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

/**
 * Handles email verification flow:
 * 1. On signup, generates a token stored in Redis (24h TTL)
 * 2. Sends verification email with link
 * 3. On verification, marks user as email_verified = true
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String appBaseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String REDIS_PREFIX = "email-verify:";
    private static final String RESET_PREFIX = "pwd-reset:";
    private static final Duration TOKEN_TTL = Duration.ofHours(24);
    private static final Duration RESET_TTL = Duration.ofHours(1);

    /**
     * Sends a verification email to the newly registered user.
     */
    public void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();
        String redisKey = REDIS_PREFIX + token;

        // Store token → userId in Redis with 24h TTL
        redisTemplate.opsForValue()
                .set(redisKey, String.valueOf(user.getId()), TOKEN_TTL);

        String verifyLink = appBaseUrl + "/verify-email?token=" + token;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Verify your asq account");
            message.setText("Hi " + user.getName() + ",\n\n"
                    + "Welcome to asq! Please verify your email by clicking the link below:\n\n"
                    + verifyLink + "\n\n"
                    + "This link expires in 24 hours.\n\n"
                    + "If you did not create this account, please ignore this email.");
            mailSender.send(message);
            log.info("Verification email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", user.getEmail(), e.getMessage());
            // Don't fail signup if email sending fails — user can request resend
        }
    }

    /**
     * Verifies the email token and marks the user as verified.
     */
    @Transactional
    public void verifyEmail(String token) {
        String redisKey = REDIS_PREFIX + token;

        String userId = redisTemplate.opsForValue().get(redisKey);
        if (userId == null) {
            throw new BusinessRuleException("INVALID_VERIFICATION_TOKEN",
                    "Verification token is invalid or has expired. Please request a new one.");
        }

        User user = userRepository.findById(Long.parseLong(userId)).orElse(null);
        if (user == null) {
            throw new BusinessRuleException("USER_NOT_FOUND", "User not found for verification token.");
        }

        if (user.isEmailVerified()) {
            // Already verified — clean up and return silently
            redisTemplate.delete(redisKey);
            return;
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        // Clean up token
        redisTemplate.delete(redisKey);
        log.info("Email verified for user {}", user.getEmail());
    }

    /**
     * Resends the verification email for a user who hasn't verified yet.
     */
    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.isEmailVerified()) {
            // Don't leak user existence — silently return
            return;
        }
        sendVerificationEmail(user);
    }

    /**
     * Initiates the forgot-password flow. Generates a one-time token (1h TTL),
     * stores it in Redis, and emails a reset link to the user.
     * Silently returns if the email doesn't exist (prevents user enumeration).
     */
    public void sendPasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            // Don't leak whether the email exists
            log.debug("Password reset requested for unknown email");
            return;
        }

        String token = UUID.randomUUID().toString();
        String redisKey = RESET_PREFIX + token;

        redisTemplate.opsForValue()
                .set(redisKey, String.valueOf(user.getId()), RESET_TTL);

        String resetLink = appBaseUrl + "/reset-password?token=" + token;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Reset your asq password");
            message.setText("Hi " + user.getName() + ",\n\n"
                    + "We received a request to reset your password. "
                    + "Click the link below to choose a new password:\n\n"
                    + resetLink + "\n\n"
                    + "This link expires in 1 hour.\n\n"
                    + "If you did not request a password reset, please ignore this email. "
                    + "Your password will remain unchanged.");
            mailSender.send(message);
            log.info("Password reset email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    /**
     * Validates the reset token and updates the user's password.
     * The token is single-use — it is deleted immediately after successful reset.
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        String redisKey = RESET_PREFIX + token;

        String userId = redisTemplate.opsForValue().get(redisKey);
        if (userId == null) {
            throw new BusinessRuleException("INVALID_RESET_TOKEN",
                    "Password reset token is invalid or has expired. Please request a new one.");
        }

        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new BusinessRuleException("USER_NOT_FOUND",
                        "User not found for reset token."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Invalidate the token — single use
        redisTemplate.delete(redisKey);
        log.info("Password reset completed for user {}", user.getEmail());
    }
}
