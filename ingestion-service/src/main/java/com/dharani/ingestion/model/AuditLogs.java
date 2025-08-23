package com.dharani.ingestion.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor               // <- needed by Spring Data
@AllArgsConstructor              // <- full-args if you want it
@Document(collection = "audit_logs")
public class AuditLogs {

    @Id
    private String id;                       // Mongo _id

    @Indexed
    private String logId;                    // correlation across services

    private String stage;                    // Validation/Normalization/...
    private String service;

    @Indexed
    private String messageId;

    private String status;                   // SUCCESS/FAILED/DUPLICATE/...

    private List<String> errors;
    private List<String> rulesChecked;
    private List<String> matchedRules;

    private Map<String,Object> details;

    // optional: pretty message if you want to store it
    private String message;

    @CreatedDate
    private Instant createdAt;

    // ✅ Convenience constructor you can use from controller
    public AuditLogs(String logId,
                     String stage,
                     String service,
                     String messageId,
                     String status,
                     Map<String, Object> details) {
        this.logId = logId;
        this.stage = stage;
        this.service = service;
        this.messageId = messageId;
        this.status = status;
        this.details = details;
    }
}
