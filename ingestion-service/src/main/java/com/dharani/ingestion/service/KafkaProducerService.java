package com.dharani.ingestion.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, String key, String message) {
        CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(topic, key, message);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("❌ Failed to send message: topic={} key={}", topic, key, ex);
            } else {
                RecordMetadata meta = result.getRecordMetadata();
                log.info("✅ Message sent: topic={} partition={} offset={} key={}",
                        meta.topic(), meta.partition(), meta.offset(), key);
            }
        });
    }
}
