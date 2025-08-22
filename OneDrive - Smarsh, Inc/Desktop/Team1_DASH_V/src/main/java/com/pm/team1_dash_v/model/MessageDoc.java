package com.pm.team1_dash_v.model;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class MessageDoc {
    @Builder.Default
    private Instant timestamp = Instant.now();
    private String messageId;
    private String source;      // Slack/Email/API
    private String channel;
    private String userId;
    private String dedupeHash;  // sha256 over canonical payload
    private String status;      // ingested/normalized/flagged
    private String text;
    private Map<String,Object> metadata;
}
