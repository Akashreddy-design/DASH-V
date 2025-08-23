package com.complyvault.policy_engine.model.policy;

import lombok.Data;

/**
 * Matches your uploaded JSON shape.
 * Example file will live under: src/main/resources/policies/subject_policy.json
 */
@Data
public class Policy {
    private String ruleId;
    private int version;
    private PolicyType type; // REGEX
    private When when;       // simple condition block for now
    private String field;    // which CanonicalMessage field to inspect (e.g., "subject")
    private String pattern;  // regex pattern (for REGEX type)
    private String description;

    @Data
    public static class When {
        private String networkEquals; // e.g., "email"
    }
}
