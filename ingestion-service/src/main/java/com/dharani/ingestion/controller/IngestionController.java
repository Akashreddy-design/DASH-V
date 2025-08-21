package com.dharani.ingestion.controller;

import com.dharani.ingestion.service.MessageProcessorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/ingest")
public class IngestionController {

    private final MessageProcessorService messageProcessorService;

    // In-memory dedupe. Switch to Redis/DB later if you need persistence across restarts.
    private final Set<String> seenMessageIds = ConcurrentHashMap.newKeySet();

    public IngestionController(MessageProcessorService messageProcessorService) {
        this.messageProcessorService = messageProcessorService;
    }

    @PostMapping("/email")
    public ResponseEntity<String> ingestEmail(@RequestBody String rawEmailJson) {
        return ingestInternal(rawEmailJson, "email");
    }

    @PostMapping("/slack")
    public ResponseEntity<String> ingestSlack(@RequestBody String rawSlackJson) {
        return ingestInternal(rawSlackJson, "slack");
    }

    // --- minimal shared handler ---
    private ResponseEntity<String> ingestInternal(String rawJson, String messageType) {
        String messageId;
        try {
            // Compute the messageId deterministically (or use provided one)
            messageId = messageProcessorService.previewMessageId(rawJson);

            // Duplicate check
            if (!seenMessageIds.add(messageId)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Duplicate message detected with id=" + messageId);
            }

            try {
                // Validate + publish
                messageProcessorService.processMessage(rawJson, messageType);
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(cap(messageType) + " payload accepted with id=" + messageId);

            } catch (IllegalArgumentException e) {
                // Validation or client error—allow retry by unmarking
                seenMessageIds.remove(messageId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

            } catch (Exception e) {
                // Server error—allow retry by unmarking
                seenMessageIds.remove(messageId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to ingest message: " + e.getMessage());
            }

        } catch (IllegalArgumentException e) {
            // Invalid JSON, missing type, etc.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private static String cap(String s) { return (s == null || s.isEmpty()) ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1); }
}
