package com.dharani.ingestion.controller;

import com.dharani.ingestion.model.AuditLogs;
import com.dharani.ingestion.service.AuditService;
import com.dharani.ingestion.service.MessageProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/ingest")
@RequiredArgsConstructor   // <-- lets Spring inject final fields
public class IngestionController {

    private final MessageProcessorService messageProcessorService;
    private final AuditService auditService;           // <-- DI added

    // In-memory dedupe. Switch to Redis/DB later if you need persistence across restarts.
    private final Set<String> seenMessageIds = ConcurrentHashMap.newKeySet();

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
        String logId = UUID.randomUUID().toString(); // correlation id per request

        try {
            // Compute the messageId deterministically (or use provided one)
            messageId = messageProcessorService.previewMessageId(rawJson);

            // Duplicate check
            if (!seenMessageIds.add(messageId)) {
                // ✅ write DUPLICATE audit using the constructor
                auditService.log(new AuditLogs(
                        logId,
                        "dedupe",
                        "ingestion",
                        messageId,
                        "DUPLICATE",
                        Map.of("type", messageType)
                ));
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Duplicate message detected with id=" + messageId);
            }

            try {
                // Validate + publish
                messageProcessorService.processMessage(rawJson, messageType);

                // ✅ write SUCCESS audit using the constructor
                auditService.log(new AuditLogs(
                        logId,
                        "process",
                        "ingestion",
                        messageId,
                        "SUCCESS",
                        Map.of("type", messageType, "size", rawJson.length())
                ));

                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(cap(messageType) + " payload accepted with id=" + messageId);

            } catch (IllegalArgumentException e) {
                // Validation or client error—allow retry by unmarking
                seenMessageIds.remove(messageId);

                // ✅ write FAILED (validate) audit
                AuditLogs audit = new AuditLogs(
                        logId,
                        "validate",
                        "ingestion",
                        messageId,
                        "FAILED",
                        Map.of("type", messageType, "reason", e.getMessage())
                );
                auditService.log(audit);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

            } catch (Exception e) {
                // Server error—allow retry by unmarking
                seenMessageIds.remove(messageId);

                // ✅ write FAILED (process) audit
                AuditLogs audit = new AuditLogs(
                        logId,
                        "process",
                        "ingestion",
                        messageId,
                        "FAILED",
                        Map.of("type", messageType, "reason", e.toString())
                );
                auditService.log(audit);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to ingest message: " + e.getMessage());
            }

        } catch (IllegalArgumentException e) {
            // Invalid JSON, missing type, etc.
            // ✅ write FAILED (parse) audit — messageId may be null
            AuditLogs audit = new AuditLogs(
                    logId,
                    "parse",
                    "ingestion",
                    null,
                    "FAILED",
                    Map.of("type", messageType, "reason", e.getMessage())
            );
            auditService.log(audit);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private static String cap(String s) {
        return (s == null || s.isEmpty()) ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
