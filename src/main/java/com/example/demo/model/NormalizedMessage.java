package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NormalizedMessage {
    private String messageId;
    private String tenantId;
    private String sender;
    private String channel; // email/slack
    private String content;
    private String timestamp;
}
