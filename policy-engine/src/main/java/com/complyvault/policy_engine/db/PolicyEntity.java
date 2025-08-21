package com.complyvault.policy_engine.db;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "policies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PolicyEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;                 // internal id

    @Column(nullable = false, unique = true)
    private String ruleId;             // external rule id

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false)
    private String type;               // "REGEX" for now

    private String networkEquals;      // simple WHEN clause

    @Column(nullable = false)
    private String field;              // e.g., subject/body

    @Column(nullable = false, length = 4000)
    private String pattern;            // regex

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
