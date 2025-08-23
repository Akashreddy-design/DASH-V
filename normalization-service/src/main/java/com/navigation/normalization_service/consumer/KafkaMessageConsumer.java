package com.navigation.normalization_service.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navigation.normalization_service.model.AuditLogs;
import com.navigation.normalization_service.service.AuditService;
import com.navigation.normalization_service.service.NormalizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessageConsumer {

    private final NormalizationService normalizationService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AuditService auditService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${app.topics.ingest:ingest.v1}")
    private String ingestTopic;

    @Value("${app.topics.dlq:ingest.dlq}")
    private String dlqTopic;

    @KafkaListener(
            topics = "${app.topics.ingest:ingest.v1}",
            groupId = "${spring.kafka.consumer.group-id:normalization-group}"
    )
    public void consume(ConsumerRecord<String, String> record) {
        final String key = record.key();               // intended messageId
        final String rawJson = record.value();

        // Best-effort enrichment for audits
        String messageId = key;
        String tenantId  = null;
        String network   = null;
        try {
            JsonNode root = mapper.readTree(rawJson);
            if (messageId == null || messageId.isBlank()) {
                messageId = text(root, "messageId");  // may still be blank
            }
            tenantId = text(root, "tenantId");
            network  = text(root, "network");
        } catch (Exception ignore) { /* keep nulls if parse fails */ }

        try {
            normalizationService.processAndStore(rawJson);

            // ✅ Audit: SUCCESS
            AuditLogs successAudit = AuditLogs.builder()
                    .logId(UUID.randomUUID().toString())
                    .tenantId(tenantId)
                    .stage("Normalization")
                    .service("normalization-service")
                    .messageId(messageId)
                    .status("SUCCESS")
                    .errors(Collections.emptyList())
                    .rulesChecked(Collections.emptyList())
                    .matchedRules(Collections.emptyList())
                    .details(Map.of(
                            "sourceTopic", record.topic(),
                            "partition", record.partition(),
                            "offset", record.offset(),
                            "network", network
                            // , "rawMessage", rawJson   // uncomment if you really want full payload
                    ))
                    .build();

            auditService.log(successAudit);

            log.info("✅ Consumed & normalized key={} topic={} partition={} offset={}",
                    messageId, record.topic(), record.partition(), record.offset());

        } catch (Exception e) {
            log.error("❌ Normalization failed for key={} topic={} partition={} offset={}. Routing to DLQ={}",
                    messageId, record.topic(), record.partition(), record.offset(), dlqTopic, e);

            // ❌ Audit: FAILED
            AuditLogs failedAudit = AuditLogs.builder()
                    .logId(UUID.randomUUID().toString())
                    .tenantId(tenantId)
                    .stage("Normalization")
                    .service("normalization-service")
                    .messageId(messageId)
                    .status("FAILED")
                    .errors(Collections.singletonList(e.getClass().getSimpleName() + ": " + e.getMessage()))
                    .rulesChecked(Collections.emptyList())
                    .matchedRules(Collections.emptyList())
                    .details(Map.of(
                            "sourceTopic", record.topic(),
                            "partition", record.partition(),
                            "offset", record.offset(),
                            "network", network,
                            "dlq", dlqTopic
                            // , "rawMessage", rawJson
                    ))
                    .build();

            auditService.log(failedAudit);

            // Best-effort DLQ
            try {
                kafkaTemplate.send(dlqTopic, messageId, rawJson);
            } catch (Exception dlqEx) {
                log.error("DLQ publish failed for key={} to topic={}", messageId, dlqTopic, dlqEx);
                // Optional: audit DLQ failure as well
                auditService.log(AuditLogs.builder()
                        .logId(UUID.randomUUID().toString())
                        .tenantId(tenantId)
                        .stage("Normalization")
                        .service("normalization-service")
                        .messageId(messageId)
                        .status("DLQ_SEND_FAILED")
                        .errors(Collections.singletonList(dlqEx.getClass().getSimpleName() + ": " + dlqEx.getMessage()))
                        .details(Map.of("dlq", dlqTopic))
                        .build());
            }
        }
    }

    private static String text(JsonNode n, String field) {
        if (n == null) return null;
        JsonNode v = n.get(field);
        return (v == null || v.isNull()) ? null : v.asText(null);
    }
}
