package com.navigation.normalization_service.controller;

import com.pm.audit.model.AuditLogs;
import com.pm.audit.service.AuditService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/Normalization")
public class NormalizationController {
    private final AuditService auditService;
    public NormalizationController(
                               AuditService auditService) {

        this.auditService = auditService;
    }
    private void logAudit(String action, String messageId, String status, String requestPayload) {
        AuditLogs log = new AuditLogs();
        log.setServiceName("normalization-service");
        log.setAction(action);
        log.setRequestPayload(requestPayload);
        log.setResponseStatus(status);
        log.setTraceId(messageId);   // reuse messageId as trace
        log.setTimestamp(Instant.now());

        auditService.save(log);
    }

    private static String cap(String s) {
        return (s == null || s.isEmpty()) ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

}
