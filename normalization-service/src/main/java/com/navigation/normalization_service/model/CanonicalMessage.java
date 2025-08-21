package com.navigation.normalization_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "messages")
public class CanonicalMessage {

    @Id
    private String id;

    private String messageId;
    private String tenantId;
    private String network;
    private String sender;
    private Instant timestamp;
    private String subject;
    private String body;
    private List<String> recipients;
    private String team;
    private String rawFileRef;
    private String rawPayload;
}
