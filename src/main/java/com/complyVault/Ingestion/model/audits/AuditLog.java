package com.complyVault.Ingestion.model.audits;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data
@Document(collection = "ingestion_audits")
public class AuditLog {
    @Id
    private String id;
    private String tenantId;
    private String messageId;
    private String eventType;
    private String network;
    private Instant timestamp;
}