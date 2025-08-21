package com.complyvault.policy_engine.db;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "violations",
        indexes = {
                @Index(name = "idx_violations_detected_at", columnList = "detected_at"),
                @Index(name = "idx_violations_rule_id", columnList = "rule_id"),
                @Index(name = "idx_violations_message_id", columnList = "message_id"),
                @Index(name = "idx_violations_network", columnList = "network")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class
ViolationEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "message_id", nullable = false)
    private String messageId;

    @Column(name = "rule_id", nullable = false)
    private String ruleId;

    @Column(nullable = false)
    private String field;                // e.g., subject/body

    @Column(name = "matched_sample", length = 1000)
    private String matchedSample;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(nullable = false)
    private String network;              // from message.network for filtering

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @Enumerated(EnumType.STRING)
    private Status status = Status.FLAGGED;

    public enum Status {
        FLAGGED,
        REVIEWED
    }

}

