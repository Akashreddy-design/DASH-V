package com.common.audit.aspect;

import com.common.audit.model.AuditLogs;
import com.common.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnClass(KafkaListener.class)
@ConditionalOnProperty(prefix = "audit.kafka", name = "enabled", havingValue = "true")
public class KafkaAuditAspect {

    private final AuditService auditService;

    @Around("@annotation(kafkaListener)")
    public Object aroundKafkaListener(ProceedingJoinPoint pjp, KafkaListener kafkaListener) throws Throwable {
        String payload = Arrays.stream(pjp.getArgs())
                .filter(arg -> arg instanceof Message<?> || arg instanceof String || arg instanceof byte[])
                .findFirst().map(Object::toString).orElse(null);

        String svc = pjp.getTarget().getClass().getSimpleName();

        auditService.save(new AuditLogs(null, svc, "kafka-consume", payload, "RECEIVED", null, Instant.now()));
        try {
            Object out = pjp.proceed();
            auditService.save(new AuditLogs(null, svc, "kafka-consume", payload, "PROCESSED", null, Instant.now()));
            return out;
        } catch (Exception e) {
            auditService.save(new AuditLogs(null, svc, "kafka-consume", payload, "FAILED: " + e.getMessage(), null, Instant.now()));
            throw e;
        }
    }
}
