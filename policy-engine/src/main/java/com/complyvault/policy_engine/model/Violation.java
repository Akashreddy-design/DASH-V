package com.complyvault.policy_engine.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * What we produce when a policy matches.
 * DB integration will come later; for now it’s an in-memory object.
 */
@Data
@Builder
public class Violation {
    private UUID id;
    private String messageId;
    private String ruleId;
    private String field;          // which field matched, e.g., "subject"
    private String matchedSample;  // small snippet or the exact match (optional, filled later)
    private String description;    // from policy
    private Instant detectedAt;
}
