package com.complyVault.Ingestion.model;

import lombok.Data;
import java.time.Instant;

@Data
public class SlackMessageRaw {
    private String messageId;   // may already exist in input
    private String tenantId;
    private String network;     // "slack"
    private String user;
    private String text;
    private Instant timestamp;
    private String team;
    private String channel;

}
