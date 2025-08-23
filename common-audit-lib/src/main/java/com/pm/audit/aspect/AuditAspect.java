package com.pm.audit.aspect;

import com.pm.audit.model.AuditLogs;
import com.pm.audit.service.AuditService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @AfterReturning(value = "execution(* com.*..controller..*(..))", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        AuditLogs log = new AuditLogs();
        log.setServiceName(joinPoint.getTarget().getClass().getSimpleName());
        log.setAction(joinPoint.getSignature().getName());
        log.setResponseStatus("SUCCESS");
        log.setTimestamp(Instant.now());
        auditService.save(log);
    }

    @AfterThrowing(value = "execution(* com.*..controller..*(..))", throwing = "ex")
    public void logError(JoinPoint joinPoint, Exception ex) {
        AuditLogs log = new AuditLogs();
        log.setServiceName(joinPoint.getTarget().getClass().getSimpleName());
        log.setAction(joinPoint.getSignature().getName());
        log.setResponseStatus("FAILED: " + ex.getMessage());
        log.setTimestamp(Instant.now());
        auditService.save(log);
    }
}
