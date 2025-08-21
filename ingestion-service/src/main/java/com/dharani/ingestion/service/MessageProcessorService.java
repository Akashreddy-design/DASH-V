// src/main/java/com/dharani/ingestion/service/MessageProcessorService.java

package com.dharani.ingestion.service;

import com.dharani.ingestion.validation.JsonSchemaValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class MessageProcessorService {

    private final KafkaProducerService producerService;
    private final ObjectMapper objectMapper;

    public MessageProcessorService(KafkaProducerService producerService) {
        this.producerService = producerService;
        this.objectMapper = new ObjectMapper();
    }

    public void processMessage(String rawJson, String messageType) {
        // Step 1: Validate the JSON (against schema)
        validateJson(rawJson, messageType);

        try {
            JsonNode rootNode = objectMapper.readTree(rawJson);
            ObjectNode objectNode = (ObjectNode) rootNode;

            // Step 2: Handle or generate a stable and unique ID
            if (!objectNode.has("messageId") || objectNode.get("messageId").asText().isEmpty()) {
                String stableMessageId = generateStableMessageId(objectNode);
                objectNode.put("messageId", stableMessageId);
            }

            // Step 3: Publish to Kafka
            String payloadWithId = objectMapper.writeValueAsString(objectNode);
            String messageId = objectNode.get("messageId").asText();

// Use KafkaProducerService so logs and error handling are centralized
            producerService.sendMessage("ingest.v1", messageId, payloadWithId);

        } catch (Exception e) {
            throw new RuntimeException("Failed to process and publish message.", e);
        }
    }

    private void validateJson(String rawJson, String messageType) {
        try {
            JsonSchemaValidator.validate(rawJson, messageType);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON validation failed: " + e.getMessage());
        }
    }


    private String generateStableMessageId(ObjectNode node) {
        try {
            // Pick a few stable fields (falling back to empty if missing)
            String tenantId  = node.hasNonNull("tenantId") ? node.get("tenantId").asText() : "";
            String network   = node.hasNonNull("network") ? node.get("network").asText() : "";
            String sender    = node.hasNonNull("sender") ? node.get("sender").asText() : "";
            String subject   = node.hasNonNull("subject") ? node.get("subject").asText() : "";
            String timestamp = node.hasNonNull("timestamp") ? node.get("timestamp").asText() : "";

            // Concatenate into one fingerprint string
            String fingerprint = tenantId + "|" + network + "|" + sender + "|" + subject + "|" + timestamp;

            // Hash with SHA-256 for stability
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fingerprint.getBytes(StandardCharsets.UTF_8));

            // Return first 32 hex chars (shorter but still unique enough)
            return HexFormat.of().formatHex(hash, 0, 16);

        } catch (Exception e) {
            // Fallback in case hashing fails
            return UUID.randomUUID().toString();
        }
    }
    // In MessageProcessorService.java (add this method)
    public String previewMessageId(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            if (!(root instanceof ObjectNode obj)) {
                throw new IllegalArgumentException("Payload must be a JSON object at the root.");
            }

            // If client provided messageId, use it; else generate like in processMessage()
            if (obj.hasNonNull("messageId") && !obj.get("messageId").asText().isBlank()) {
                return obj.get("messageId").asText().trim();
            }
            // your simple generator (the one we wrote earlier)
            return generateStableMessageId(obj);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON: " + e.getMessage());
        }
    }

}