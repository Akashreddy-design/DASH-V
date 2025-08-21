// src/main/java/com/dharani/ingestion/service/MessageProcessorService.java

package com.dharani.ingestion.service;

import com.dharani.ingestion.validation.JsonSchemaValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MessageProcessorService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public MessageProcessorService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
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
                String stableMessageId = UUID.randomUUID().toString();
                objectNode.put("messageId", stableMessageId);
            }

            // Step 3: Publish to Kafka
            String payloadWithId = objectMapper.writeValueAsString(objectNode);
            kafkaTemplate.send("ingest.v1", payloadWithId);

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
}