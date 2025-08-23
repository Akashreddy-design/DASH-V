package com.complyvault.policy_engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Minimal canonical schema we’ll test against now.
 * We can expand later when Normalizer’s schema is finalized.
 */

@Data // or getters/setters
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CanonicalMessage {
    private String id;          // (not required by evaluator)
    private String messageId;
    private String tenantId;
    private String network;     // "email" | "slack" | ...
    private String sender;
    private Instant timestamp;
    private String subject;
    private String body;        // <-- use this for Slack text too
    private List<String> recipients;
    private String team;
    private String rawFileRef;

}