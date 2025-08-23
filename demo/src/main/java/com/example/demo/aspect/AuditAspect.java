// com/common/audit/aspect/AuditAspect.java
package com.example.demo.aspect;

import com.example.demo.model.AuditLogs;
import com.example.demo.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

@Component
@Aspect
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "audit.http", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditAspect {

    private final AuditService auditService;
    private final Optional<io.micrometer.tracing.Tracer> tracerOpt = Optional.empty(); // inject if on classpath

    @AfterReturning(value = "execution(* ..controller..*(..))", returning = "result")
    public void logAfter(JoinPoint jp, Object result) {
        AuditLogs log = base(jp);
        log.setResponseStatus("SUCCESS");
        auditService.save(log);
    }

    @AfterThrowing(value = "execution(* ..controller..*(..))", throwing = "ex")
    public void logError(JoinPoint jp, Exception ex) {
        AuditLogs log = base(jp);
        log.setResponseStatus("FAILED: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
        auditService.save(log);
    }

    private AuditLogs base(JoinPoint jp) {
        AuditLogs log = new AuditLogs();
        log.setServiceName(jp.getTarget().getClass().getSimpleName());
        log.setAction(jp.getSignature().getName());
        log.setRequestPayload(extractBody());
        log.setTraceId(tracerOpt.map(t -> {
            var span = t.currentSpan();
            return span != null ? span.context().traceId() : null;
        }).orElse(null));
        log.setTimestamp(Instant.now());
        return log;
    }

    private String extractBody() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        Object requestObj = attrs.resolveReference(RequestAttributes.REFERENCE_REQUEST);
        if (requestObj instanceof ContentCachingRequestWrapper req) {
            byte[] buf = req.getContentAsByteArray();
            if (buf.length > 0) return new String(buf, StandardCharsets.UTF_8);
        }
        return null;
    }
}
