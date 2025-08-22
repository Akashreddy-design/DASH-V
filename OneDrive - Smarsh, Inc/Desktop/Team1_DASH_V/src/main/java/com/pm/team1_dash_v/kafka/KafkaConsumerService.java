package com.pm.team1_dash_v.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Stub: later this will apply regex policies and persist.
 */
@Service
public class KafkaConsumerService {

    @KafkaListener(topics = {"${cv.kafka.topic.email}", "${cv.kafka.topic.slack}"}, groupId = "compliance-consumers")
    public void consume(ConsumerRecord<String, String> rec) {
        // TODO: Apply regex/policy evaluation, write to Mongo/ES, emit audit logs
        System.out.printf("Consumed %s -> %s%n", rec.key(), rec.value());
    }
}