package com.survey_engine.common.auditing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey_engine.common.models.AuditLog;
import com.survey_engine.common.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Aspect for auditing method executions annotated with {@link Auditable}.
 * This class intercepts method calls, logs relevant information, and persists it to the database.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Defines the pointcut for all methods annotated with {@code @Auditable}.
     */
    @Pointcut("@annotation(Auditable)")
    public void auditablePointcut() {
    }

    /**
     * Advice that wraps around methods annotated with {@code @Auditable}.
     * It captures method execution details, logs them, and handles success or failure status.
     *
     * @param joinPoint The join point for the advised method.
     * @return The result of the original method execution.
     * @throws Throwable if the original method throws an exception.
     */
    @Around("auditablePointcut()")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = (authentication != null) ? authentication.getName() : "ANONYMOUS";

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Auditable auditable = method.getAnnotation(Auditable.class);
        String action = auditable.action().isEmpty() ? method.getName() : auditable.action();

        String parameters = getParameters(joinPoint);
        Object result;
        try {
            result = joinPoint.proceed();
            saveAuditLog(actor, action, parameters, "SUCCESS");
        } catch (Throwable throwable) {
            saveAuditLog(actor, action, parameters, "FAILURE");
            log.error("Audited method '{}' failed for actor '{}'", action, actor, throwable);
            throw throwable;
        }

        return result;
    }

    /**
     * Saves the audit log entry to the database.
     *
     * @param actor      The user who performed the action.
     * @param action     The description of the action.
     * @param parameters The parameters of the method.
     * @param status     The execution status (SUCCESS/FAILURE).
     */
    private void saveAuditLog(String actor, String action, String parameters, String status) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setActor(actor);
            auditLog.setAction(action);
            auditLog.setParameters(parameters);
            auditLog.setStatus(status);
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("CRITICAL: Failed to save audit log for actor '{}' and action '{}'", actor, action, e);
            // This failure should not interrupt the main application flow.
        }
    }

    /**
     * Serializes the method parameters to a JSON string, redacting sensitive information.
     *
     * @param joinPoint The join point for the advised method.
     * @return A JSON string representing the method parameters.
     */
    private String getParameters(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] parameterNames = signature.getParameterNames();
            Object[] parameterValues = joinPoint.getArgs();
            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < parameterNames.length; i++) {
                if (isSensitive(parameterNames[i])) {
                    params.put(parameterNames[i], "******");
                } else {
                    params.put(parameterNames[i], parameterValues[i]);
                }
            }
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            log.warn("Could not serialize parameters for audit log", e);
            return "{\"error\":\"Could not serialize parameters\"}";
        }
    }

    /**
     * Checks if a parameter name is considered sensitive.
     *
     * @param paramName The name of the parameter.
     * @return true if the parameter is sensitive, false otherwise.
     */
    private boolean isSensitive(String paramName) {
        String lowerCaseParamName = paramName.toLowerCase();
        return lowerCaseParamName.contains("password") || lowerCaseParamName.contains("token") || lowerCaseParamName.contains("jwt");
    }
}