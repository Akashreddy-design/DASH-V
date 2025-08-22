package com.pm.team1_dash_v.service;

import com.pm.team1_dash_v.model.AuditLogs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class NormalizationService {
    private final com.pm.team1_dash_v.service.AuditService auditService;

    public Map<String,Object> normalize(String messageId, Map<String,Object> payload) {
        String text = String.valueOf(payload.get("text"));
        Map<String,Object> canonical = Map.of(
                "text", text,
                "source", payload.getOrDefault("source","API"),
                "channel", payload.getOrDefault("channel","inbox"),
                "userId", payload.getOrDefault("userId","unknown"),
                "metadata", payload.getOrDefault("metadata", Map.of())
        );

        auditService.log(AuditLogs.builder()
                .logId(UUID.randomUUID().toString())
                .stage("Normalization").service("NormalizationService")
                .messageId(messageId).status("passed")
                .details(Map.of("convertedSchema","CanonicalSchemaV1"))
                .build());

        return canonical;
    }
}
