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
            String tenantId = node.hasNonNull("tenantId") ? node.get("tenantId").asText() : "";
            String network  = node.hasNonNull("network") ? node.get("network").asText() : "";

            StringBuilder fingerprintBuilder = new StringBuilder();
            fingerprintBuilder.append(tenantId).append("|").append(network);

            if ("email".equalsIgnoreCase(network) && node.hasNonNull("payload")) {
                ObjectNode payload = (ObjectNode) node.get("payload");
                String from     = payload.hasNonNull("from") ? payload.get("from").asText() : "";
                String to       = payload.hasNonNull("to") && payload.get("to").isArray()
                        ? payload.get("to").toString() : "";
                String subject  = payload.hasNonNull("subject") ? payload.get("subject").asText() : "";
                String body     = payload.hasNonNull("body") ? payload.get("body").asText() : "";
                String sentAt   = payload.hasNonNull("sentAt") ? payload.get("sentAt").asText() : "";

                fingerprintBuilder.append("|").append(from)
                        .append("|").append(to)
                        .append("|").append(subject)
                        .append("|").append(body)
                        .append("|").append(sentAt);

            } else if ("slack".equalsIgnoreCase(network)) {
                String user     = node.hasNonNull("user") ? node.get("user").asText() : "";
                String text     = node.hasNonNull("text") ? node.get("text").asText() : "";
                String timestamp= node.hasNonNull("timestamp") ? node.get("timestamp").asText() : "";
                String team     = node.hasNonNull("team") ? node.get("team").asText() : "";
                String channel  = node.hasNonNull("channel") ? node.get("channel").asText() : "";
                String rawRef   = node.hasNonNull("rawReference") ? node.get("rawReference").asText() : "";

                fingerprintBuilder.append("|").append(user)
                        .append("|").append(text)
                        .append("|").append(timestamp)
                        .append("|").append(team)
                        .append("|").append(channel)
                        .append("|").append(rawRef);
            } else {
                // Fallback: include everything we can
                fingerprintBuilder.append("|").append(node.toString());
            }

            // Create SHA-256 hash of fingerprint
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fingerprintBuilder.toString().getBytes(StandardCharsets.UTF_8));

            // Return first 16 hex chars
            return HexFormat.of().formatHex(hash, 0, 16);

        } catch (Exception e) {
            // Last fallback: hash full JSON
            try {
                String jsonString = objectMapper.writeValueAsString(node);
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(jsonString.getBytes(StandardCharsets.UTF_8));
                return HexFormat.of().formatHex(hash, 0, 16);
            } catch (Exception fallbackException) {
                return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            }
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




