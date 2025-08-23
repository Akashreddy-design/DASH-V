package com.navigation.normalization_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;


import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    // Handles email 'to' and slack 'channel'
    private List<String> recipients;

    // Slack team name
    private String team;

    private String rawFileRef;

}
