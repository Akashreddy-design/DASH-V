package com.pm.audit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")
public class AuditLogs {

    @Id
    private String id;

    private String serviceName;
    private String action;
    private String requestPayload;
    private String responseStatus;
    private String traceId;
    private Instant timestamp;
}
