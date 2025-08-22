package com.pm.team1_dash_v.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.annotation.CreatedDate;
import java.time.Instant;
import java.util.List;
import java.util.Map;


@Data
@Builder
@Document(collection = "audit_logs")
public class AuditLogs {
    @Id
    private String id;                       // Mongo _id
    @Indexed
    private String logId;                    // same as in AuditDoc
    private String stage;                    // Validation/Normalization/...
    private String service;
    @Indexed
    private String messageId;
    private String status;                   // passed/failed/duplicate/flagged
    private List<String> errors;
    private List<String> rulesChecked;
    private List<String> matchedRules;
    private Map<String,Object> details;
    @CreatedDate
    private Instant createdAt;
    // auto-set if auditing enabled
    public Long getTimestamp() {
        return System.currentTimeMillis();
    }

    // getters/setters/constructors omitted for brevity
}
