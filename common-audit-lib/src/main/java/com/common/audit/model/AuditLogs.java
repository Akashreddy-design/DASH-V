package com.common.audit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")
public class AuditLogs {

    @Id
    private String id;

    @Indexed
    private String serviceName;

    @Indexed
    private String action;

    private String requestPayload;

    private String responseStatus;

    @Indexed
    private String traceId;

    @Indexed
    private Instant timestamp;
}
