// src/main/java/com/dharani/normalization/service/NormalizationService.java
package com.navigation.normalization_service.service;

import com.navigation.normalization_service.model.CanonicalMessage;
import com.navigation.normalization_service.store.RawFileStore;
import com.navigation.normalization_service.store.SearchRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
//import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

@Service
public class NormalizationService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RawFileStore rawFileStore;
    private final SearchRepository searchRepository;

//    public NormalizationService(RawFileStore rawFileStore, SearchRepository searchRepository) {
//        this.rawFileStore = rawFileStore;
//        this.searchRepository = searchRepository;
//    }
//    private final SearchRepository searchRepository;

    public NormalizationService(RawFileStore rawFileStore, SearchRepository searchRepository) {
        this.rawFileStore = rawFileStore;
        this.searchRepository = searchRepository;
    }

    public void processAndStore(String rawJson) {
        try {
            JsonNode rootNode = objectMapper.readTree(rawJson);

            // Step 1: Store raw payload immutably on disk
            String rawFileRef = rawFileStore.store(rawJson);

            // Step 2: Normalize to canonical schema and include raw payload
            CanonicalMessage canonicalMessage = toCanonical(rootNode, rawFileRef, rawJson);

            // Step 3: Create a unique hash index for deduplication
            String hashIndex = createHashIndex(canonicalMessage);
            canonicalMessage.setId(hashIndex);

            // Step 4: Check for duplicates and store in Elasticsearch
            Optional<Object> existingMessage = Optional.of(searchRepository.findById(hashIndex));

            if (existingMessage.isPresent()) {
                System.out.println("Message already exists, skipping storage.");
            } else {
                searchRepository.save(canonicalMessage);
                System.out.println("Stored new message with hash index: " + hashIndex);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to process message for normalization.", e);
        }
    }

    private CanonicalMessage toCanonical(JsonNode rootNode, String rawFileRef, String rawPayload) {
        CanonicalMessage canonical = new CanonicalMessage();
        canonical.setMessageId(rootNode.path("messageId").asText());
        canonical.setTenantId(rootNode.path("tenantId").asText());
        canonical.setNetwork(rootNode.path("network").asText());
        canonical.setRawFileRef(rawFileRef);
        canonical.setRawPayload(rawPayload); // Store the full raw payload

        if ("email".equals(canonical.getNetwork())) {
            JsonNode payload = rootNode.path("payload");
            canonical.setSender(payload.path("from").asText());
            canonical.setSubject(payload.path("subject").asText());
            canonical.setBody(payload.path("body").asText());
            canonical.setTimestamp(Instant.parse(payload.path("sentAt").asText()));

            // Standardize email recipients
            List<String> recipients = new ArrayList<>();
            payload.path("to").forEach(node -> recipients.add(node.asText()));
            canonical.setRecipients(recipients);

        } else if ("slack".equals(canonical.getNetwork())) {
            canonical.setSender(rootNode.path("user").asText());
            canonical.setBody(rootNode.path("text").asText());
            canonical.setTimestamp(Instant.parse(rootNode.path("timestamp").asText()));
            canonical.setTeam(rootNode.path("team").asText());

            // Standardize slack channel as a recipient
            List<String> recipients = new ArrayList<>();
            recipients.add(rootNode.path("channel").asText());
            canonical.setRecipients(recipients);
        }
        return canonical;
    }

    private String createHashIndex(CanonicalMessage message) throws Exception {
        String data = message.getTenantId() + message.getNetwork() + message.getSender() + message.getTimestamp() + message.getBody();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
        for (byte b : encodedhash) {
            String hex = Integer.toHexString(0xff & b);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
