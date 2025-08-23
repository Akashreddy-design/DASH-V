package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NormalizedMessage {

    // Setters
    // Getters
    private String id;
    // Fixed: was returning id
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


    // Default constructor
    public NormalizedMessage() {}

}