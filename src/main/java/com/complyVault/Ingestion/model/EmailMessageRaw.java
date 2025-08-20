package com.complyVault.Ingestion.model;

import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class EmailMessageRaw {
    private String tenantId;
    private String network; // "email"
    private EmailPayload payload;

    @Data
    public static class EmailPayload {
        // payload is basically the body part of the email
        private String from;
        private List<String> to;
        private String subject;
        private String body;
        private Instant sentAt;
    }
}
