package com.complyvault.policy_engine.consumer;

import com.complyvault.policy_engine.model.AuditLogs;
import com.complyvault.policy_engine.model.CanonicalMessage;
import com.complyvault.policy_engine.service.AuditService;
import com.complyvault.policy_engine.service.PolicyEvaluator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CanonicalMessageConsumer {

    private final ObjectMapper mapper = new ObjectMapper();
    private final PolicyEvaluator policyEvaluator;
    private final AuditService auditService;

    @KafkaListener(topics = "${app.topics.canonicalPolicy}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record) {
        String json = record.value();
        String key  = record.key(); // usually messageId

        CanonicalMessage message = null;
        try {
            message = mapper.readValue(json, CanonicalMessage.class);

            final String messageId = nz(message.getMessageId(), key);
            final String tenantId  = nz(message.getTenantId(), null);

            // 📝 AUDIT: STARTED
            auditService.log(AuditLogs.builder()
                    .logId(UUID.randomUUID().toString())
                    .tenantId(tenantId)
                    .messageId(messageId)
                    .service("policy-engine")
                    .stage("Evaluation")
                    .status("STARTED")
                    .errors(Collections.emptyList())
                    .details(Map.of(
                            "sourceTopic", record.topic(),
                            "partition", record.partition(),
                            "offset", record.offset()
                    ))
                    .build());

            // ▶️ Evaluate policies
            policyEvaluator.evaluate(message);

            // ✅ AUDIT: SUCCESS
            auditService.log(AuditLogs.builder()
                    .logId(UUID.randomUUID().toString())
                    .tenantId(tenantId)
                    .messageId(messageId)
                    .service("policy-engine")
                    .stage("Evaluation")
                    .status("SUCCESS")
                    .errors(Collections.emptyList())
                    // .matchedRules(result.getMatchedRuleIds())  // if your evaluator returns them
                    .details(Map.of(
                            "sourceTopic", record.topic(),
                            "partition", record.partition(),
                            "offset", record.offset()
                    ))
                    .build());

            log.info("✅ Policy Engine evaluated id={} tenant={}", messageId, tenantId);

        } catch (Exception e) {
            // We may not have parsed the message; try to salvage ids
            String messageId = (message != null) ? message.getMessageId() : key;
            String tenantId  = (message != null) ? message.getTenantId()  : null;

            // ❌ AUDIT: FAILED
            auditService.log(AuditLogs.builder()
                    .logId(UUID.randomUUID().toString())
                    .tenantId(tenantId)
                    .messageId(messageId)
                    .service("policy-engine")
                    .stage("Evaluation")
                    .status("FAILED")
                    .errors(Collections.singletonList(e.getClass().getSimpleName() + ": " + e.getMessage()))
                    .details(Map.of(
                            "sourceTopic", record.topic(),
                            "partition", record.partition(),
                            "offset", record.offset()
                    ))
                    .build());

            log.error("❌ Failed to consume/evaluate canonical message key={} topic={} partition={} offset={}",
                    key, record.topic(), record.partition(), record.offset(), e);
        }
    }

    private static String nz(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
