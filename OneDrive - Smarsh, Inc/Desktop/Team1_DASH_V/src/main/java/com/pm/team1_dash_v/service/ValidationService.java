package com.pm.team1_dash_v.service;

import com.pm.team1_dash_v.model.AuditLogs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class ValidationService {
    private final com.pm.team1_dash_v.service.AuditService auditService;

    public boolean validate(String messageId, Map<String,Object> payload) {
        boolean ok = payload.containsKey("text");
        auditService.log(AuditLogs.builder()
                .logId(UUID.randomUUID().toString())
                .stage("Validation").service("ValidationService")
                .messageId(messageId)
                .status(ok ? "passed" : "failed")
                .errors(ok ? List.of() : List.of("Missing field: text"))
                .build());
        return ok;
    }
}
