package com.complyVault.Ingestion.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngestEvent {
    private String messageId;
    private String tenantId;
    private String network;   // email or slack
    private Object rawPayload; // original JSON
    private Instant ingestedAt;
}
