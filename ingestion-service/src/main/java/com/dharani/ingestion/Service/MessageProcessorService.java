package com.dharani.ingestion.service;

import com.dharani.ingestion.validation.JsonSchemaValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        // Step 2: Generate a stable and unique ID
        String stableMessageId = UUID.randomUUID().toString();

        // Step 3: Publish to Kafka
        try {
            JsonNode rootNode = objectMapper.readTree(rawJson);
            ((com.fasterxml.jackson.databind.node.ObjectNode) rootNode).put("messageId", stableMessageId);
            String payloadWithId = objectMapper.writeValueAsString(rootNode);

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
