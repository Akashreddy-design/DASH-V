package com.navigation.normalization_service.model;

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

    // Common correlation fields
    @Indexed
    private String logId;                 // Correlation id for a single processing attempt
    @Indexed
    private String tenantId;              // <- useful for multi-tenant queries
    @Indexed
    private String messageId;             // Stable message id across services

    // What/where
    private String service;               // e.g., "normalization-service"
    private String stage;                 // e.g., "Normalization"

    // Outcome
    @Indexed
    private String status;                // e.g., SUCCESS / FAILED / DROPPED_DUPLICATE / KAFKA_SENT

    // Optional details
    private List<String> errors;          // brief error strings, if any
    private List<String> rulesChecked;    // if you record rule ids checked
    private List<String> matchedRules;    // if you record matched rule ids
    private Map<String, Object> details;  // arbitrary extra context (topic, partition, network, etc.)
    private String message;               // pretty/human-readable line (built in AuditService)

    @CreatedDate
    private Instant createdAt;            // requires @EnableMongoAuditing
}
