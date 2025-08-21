package com.complyvault.policy_engine.consumer;

import com.complyvault.policy_engine.model.CanonicalMessage;
import com.complyvault.policy_engine.service.PolicyEvaluator;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.complyvault.policy_engine.model.policy.CanonicalMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CanonicalMessageConsumer {

    private final ObjectMapper mapper = new ObjectMapper();
    private final PolicyEvaluator policyEvaluator; // reuse your existing service

    @KafkaListener(topics = "${app.topics.canonicalPolicy}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            String json = record.value();
            CanonicalMessage message = mapper.readValue(json, CanonicalMessage.class);

            log.info("✅ Policy Engine received canonical message id={} tenant={}",
                    message.getMessageId(), message.getTenantId());

            // hand it to your policy evaluation
           policyEvaluator.evaluate(message);

        } catch (Exception e) {
            log.error("❌ Failed to consume canonical message", e);
        }
    }
}
