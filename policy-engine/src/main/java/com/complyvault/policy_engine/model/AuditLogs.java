package com.complyvault.policy_engine.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")
@CompoundIndex(name = "idx_tenant_msg_stage", def = "{'tenantId': 1, 'messageId': 1, 'stage': 1}")
public class AuditLogs {

    @Id
    private String id;                    // Mongo _id

    // Correlation
    @Indexed
    private String logId;                 // Unique per processing attempt
    @Indexed
    private String tenantId;              // Multi-tenant filtering
    @Indexed
    private String messageId;             // Stable id across services

    // What/where
    private String service;               // "policy-engine"
    private String stage;                 // e.g., "Evaluation"

    // Outcome
    @Indexed
    private String status;                // STARTED / SUCCESS / FAILED / FLAGGED / etc.

    // Optional details
    private List<String> errors;          // error messages, if any
    private List<String> rulesChecked;    // rule ids evaluated (optional)
    private List<String> matchedRules;    // rule ids matched (optional)
    private Map<String, Object> details;  // extra context (policySet, version, scores, etc.)
    private String message;               // pretty/human-readable summary (built in AuditService)

    @CreatedDate
    private Instant createdAt;

    // requires @EnableMongoAuditing
}
